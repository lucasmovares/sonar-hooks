package com.movares.sonar.hooks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.core.issue.DefaultIssue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for sending webhook notifications when issues are updated.
 */
@ServerSide
@ComputeEngineSide
public class IssueWebhookService {

    private static final Logger LOG = Loggers.get(IssueWebhookService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    private final Configuration configuration;
    private final Gson gson;

    public IssueWebhookService(Configuration configuration) {
        this.configuration = configuration;
        this.gson = new Gson();
    }

    /**
     * Sends a webhook notification for an issue update.
     * This method is asynchronous to avoid blocking the main thread.
     */
    public void sendIssueUpdateWebhook(DefaultIssue issue, String action, String projectKey, String projectName) {
        if (!isWebhookEnabled()) {
            LOG.debug("Issue webhooks are disabled");
            return;
        }

        String webhookUrl = getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            LOG.debug("No webhook URL configured");
            return;
        }

        // Send webhook asynchronously to avoid blocking
        CompletableFuture.runAsync(() -> {
            try {
                sendWebhookRequest(issue, action, projectKey, projectName, webhookUrl);
            } catch (Exception e) {
                LOG.error("Failed to send webhook for issue update", e);
            }
        });
    }

    private void sendWebhookRequest(DefaultIssue issue, String action, String projectKey, String projectName, String webhookUrl) {
        JsonObject payload = createPayload(issue, action, projectKey, projectName);
        String jsonPayload = gson.toJson(payload);

        int retryCount = getRetryCount();
        int timeout = getTimeout();

        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try {
                if (sendHttpRequest(webhookUrl, jsonPayload, timeout)) {
                    LOG.debug("Successfully sent webhook for issue {} (attempt {})", issue.key(), attempt + 1);
                    return;
                }
            } catch (Exception e) {
                LOG.warn("Failed to send webhook for issue {} (attempt {}): {}", issue.key(), attempt + 1, e.getMessage());
                
                if (attempt < retryCount) {
                    // Wait before retry (exponential backoff)
                    try {
                        Thread.sleep(1000 * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        LOG.error("Failed to send webhook for issue {} after {} attempts", issue.key(), retryCount + 1);
    }

    private boolean sendHttpRequest(String url, String jsonPayload, int timeout) throws IOException {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build()) {

            HttpPost request = new HttpPost(url);
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            request.setHeader(HttpHeaders.USER_AGENT, "SonarQube-Issue-Hooks/1.0.0");
            
            // Add HMAC signature if secret is configured
            String secret = getWebhookSecret();
            if (secret != null && !secret.trim().isEmpty()) {
                String signature = calculateHmacSignature(jsonPayload, secret);
                request.setHeader("X-Hub-Signature-256", "sha256=" + signature);
            }

            request.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                LOG.debug("Webhook response: {} - {}", statusCode, responseBody);
                
                // Consider 2xx status codes as success
                return statusCode >= 200 && statusCode < 300;
            }
        }
    }

    private JsonObject createPayload(DefaultIssue issue, String action, String projectKey, String projectName) {
        JsonObject payload = new JsonObject();
        payload.addProperty("timestamp", Instant.now().toString());
        payload.addProperty("event", "issue_updated");
        payload.addProperty("action", action);
        
        // Project information
        JsonObject project = new JsonObject();
        project.addProperty("key", projectKey);
        project.addProperty("name", projectName);
        payload.add("project", project);
        
        // Issue information
        JsonObject issueJson = new JsonObject();
        issueJson.addProperty("key", issue.key());
        issueJson.addProperty("type", issue.type() != null ? issue.type().name() : null);
        issueJson.addProperty("severity", issue.severity());
        issueJson.addProperty("status", issue.status());
        issueJson.addProperty("resolution", issue.resolution());
        issueJson.addProperty("assignee", issue.assignee());
        issueJson.addProperty("author", issue.authorLogin());
        issueJson.addProperty("component", issue.componentKey());
        issueJson.addProperty("rule", issue.ruleKey() != null ? issue.ruleKey().toString() : null);
        issueJson.addProperty("message", issue.message());
        issueJson.addProperty("line", issue.line());
        issueJson.addProperty("effort", issue.effort() != null ? issue.effort().toString() : null);
        issueJson.addProperty("creationDate", issue.creationDate() != null ? issue.creationDate().toString() : null);
        issueJson.addProperty("updateDate", issue.updateDate() != null ? issue.updateDate().toString() : null);
        issueJson.addProperty("closeDate", issue.closeDate() != null ? issue.closeDate().toString() : null);
        
        payload.add("issue", issueJson);
        
        return payload;
    }

    private String calculateHmacSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOG.error("Failed to calculate HMAC signature", e);
            return "";
        }
    }

    private boolean isWebhookEnabled() {
        return configuration.getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED).orElse(false);
    }

    private String getWebhookUrl() {
        return configuration.get(SonarHooksPlugin.WEBHOOK_URL).orElse(null);
    }

    private String getWebhookSecret() {
        return configuration.get(SonarHooksPlugin.WEBHOOK_SECRET).orElse(null);
    }

    private int getTimeout() {
        return configuration.getInt(SonarHooksPlugin.WEBHOOK_TIMEOUT).orElse(10000);
    }

    private int getRetryCount() {
        return configuration.getInt(SonarHooksPlugin.WEBHOOK_RETRY_COUNT).orElse(3);
    }
} 
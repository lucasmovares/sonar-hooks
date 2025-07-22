package com.movares.sonar.hooks;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

/**
 * Main plugin class for the SonarQube Issue Hooks Plugin.
 * This plugin extends webhook functionality to trigger when issues are updated.
 */
public class SonarHooksPlugin implements Plugin {

    public static final String PLUGIN_KEY = "sonar-hooks";
    
    // Configuration properties
    public static final String WEBHOOK_ENABLED = "sonar.hooks.webhook.enabled";
    public static final String WEBHOOK_URL = "sonar.hooks.webhook.url";
    public static final String WEBHOOK_SECRET = "sonar.hooks.webhook.secret";
    public static final String WEBHOOK_TIMEOUT = "sonar.hooks.webhook.timeout";
    public static final String WEBHOOK_RETRY_COUNT = "sonar.hooks.webhook.retryCount";
    
    @Override
    public void define(Context context) {
        // Register configuration properties
        context.addExtensions(
            // Configuration properties
            PropertyDefinition.builder(WEBHOOK_ENABLED)
                .name("Enable Issue Webhooks")
                .description("Enable webhooks for issue updates")
                .category("Issue Hooks")
                .defaultValue("false")
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_URL)
                .name("Webhook URL")
                .description("URL to send webhook notifications when issues are updated")
                .category("Issue Hooks")
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_SECRET)
                .name("Webhook Secret")
                .description("Secret key for webhook authentication (optional)")
                .category("Issue Hooks")
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_TIMEOUT)
                .name("Webhook Timeout (ms)")
                .description("Timeout for webhook HTTP requests in milliseconds")
                .category("Issue Hooks")
                .defaultValue("10000")
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_RETRY_COUNT)
                .name("Webhook Retry Count")
                .description("Number of retry attempts for failed webhook requests")
                .category("Issue Hooks")
                .defaultValue("3")
                .build(),
            
            // Plugin components
            IssueWebhookService.class,
            IssueUpdateListener.class
        );
    }
} 
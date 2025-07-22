package com.movares.sonar.hooks;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
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
    
    // Categories and subcategories
    private static final String CATEGORY = "General";
    private static final String SUBCATEGORY = "Issue Hooks";
    
    @Override
    public void define(Context context) {
        // Register configuration properties with proper subcategory structure
        context.addExtensions(
            // Webhook Configuration Properties
            PropertyDefinition.builder(WEBHOOK_ENABLED)
                .name("Enable Issue Webhooks")
                .description("Enable webhook notifications for issue updates")
                .category(CATEGORY)
                .subCategory(SUBCATEGORY)
                .type(PropertyType.BOOLEAN)
                .defaultValue("false")
                .index(1)
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_URL)
                .name("Webhook URL")
                .description("URL endpoint to send webhook notifications when issues are updated")
                .category(CATEGORY)
                .subCategory(SUBCATEGORY)
                .type(PropertyType.STRING)
                .index(2)
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_SECRET)
                .name("Webhook Secret")
                .description("Optional secret key for webhook authentication (HMAC-SHA256 signature)")
                .category(CATEGORY)
                .subCategory(SUBCATEGORY)
                .type(PropertyType.PASSWORD)
                .index(3)
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_TIMEOUT)
                .name("Webhook Timeout (ms)")
                .description("HTTP request timeout for webhook calls in milliseconds")
                .category(CATEGORY)
                .subCategory(SUBCATEGORY)
                .type(PropertyType.INTEGER)
                .defaultValue("10000")
                .index(4)
                .build(),
                
            PropertyDefinition.builder(WEBHOOK_RETRY_COUNT)
                .name("Webhook Retry Count")
                .description("Number of retry attempts for failed webhook requests")
                .category(CATEGORY)
                .subCategory(SUBCATEGORY)
                .type(PropertyType.INTEGER)
                .defaultValue("3")
                .index(5)
                .build(),
            
            // Plugin components
            IssueWebhookService.class,
            IssueUpdateListener.class
        );
    }
} 
package com.movares.sonar.hooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.config.Configuration;
import org.sonar.core.issue.DefaultIssue;

import java.util.Optional;

import static org.mockito.Mockito.*;
class IssueWebhookServiceTest {

    @Mock
    private Configuration configuration;

    @Mock
    private DefaultIssue issue;

    private IssueWebhookService webhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webhookService = new IssueWebhookService(configuration);
    }

    @Test
    void shouldNotSendWebhookWhenDisabled() {
        // Given
        when(configuration.getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED)).thenReturn(Optional.of(false));

        // When
        webhookService.sendIssueUpdateWebhook(issue, "created", "project-key", "Project Name");

        // Then
        // No HTTP request should be made (we can't easily test this without more mocking)
        verify(configuration).getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED);
    }

    @Test
    void shouldNotSendWebhookWhenUrlNotConfigured() {
        // Given
        when(configuration.getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED)).thenReturn(Optional.of(true));
        when(configuration.get(SonarHooksPlugin.WEBHOOK_URL)).thenReturn(Optional.empty());

        // When
        webhookService.sendIssueUpdateWebhook(issue, "created", "project-key", "Project Name");

        // Then
        verify(configuration).getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED);
        verify(configuration).get(SonarHooksPlugin.WEBHOOK_URL);
    }

    @Test
    void shouldPrepareForWebhookWhenEnabledAndConfigured() {
        // Given
        when(configuration.getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED)).thenReturn(Optional.of(true));
        when(configuration.get(SonarHooksPlugin.WEBHOOK_URL)).thenReturn(Optional.of("http://example.com/webhook"));
        when(configuration.get(SonarHooksPlugin.WEBHOOK_SECRET)).thenReturn(Optional.empty());
        when(configuration.getInt(SonarHooksPlugin.WEBHOOK_TIMEOUT)).thenReturn(Optional.of(10000));
        when(configuration.getInt(SonarHooksPlugin.WEBHOOK_RETRY_COUNT)).thenReturn(Optional.of(3));

        when(issue.key()).thenReturn("issue-key");
        when(issue.severity()).thenReturn("MAJOR");
        when(issue.status()).thenReturn("OPEN");

        // When
        webhookService.sendIssueUpdateWebhook(issue, "created", "project-key", "Project Name");

        // Then
        verify(configuration).getBoolean(SonarHooksPlugin.WEBHOOK_ENABLED);
        verify(configuration).get(SonarHooksPlugin.WEBHOOK_URL);
        // Additional HTTP request verification would require more sophisticated mocking
    }
} 
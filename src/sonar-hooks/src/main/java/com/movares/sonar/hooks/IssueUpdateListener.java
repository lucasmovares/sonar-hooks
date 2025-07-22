package com.movares.sonar.hooks;

import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class IssueUpdateListener implements PostProjectAnalysisTask {
    private static final Logger LOG = Loggers.get(IssueUpdateListener.class);
    private final IssueWebhookService webhookService;

    public IssueUpdateListener(IssueWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Override
    public void finished(ProjectAnalysis analysis) {
        try {
            LOG.debug("Processing project analysis completion for: {}", analysis.getProject().getKey());
            Project project = analysis.getProject();
            String projectKey = project.getKey();
            String projectName = project.getName();
            LOG.debug("Sending webhook for project analysis completion: {}", projectKey);
            triggerProjectAnalysisWebhook(projectKey, projectName, analysis);
        } catch (Exception e) {
            LOG.error("Error processing project analysis completion", e);
        }
    }

    private void triggerProjectAnalysisWebhook(String projectKey, String projectName, ProjectAnalysis analysis) {
        LOG.info("Project analysis completed for {}, webhook functionality ready", projectKey);
    }
} 
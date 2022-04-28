package com.chriscarini.jetbrains.locchangecountdetector.errorhandler;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.intellij.ide.troubleshooting.CompositeGeneralTroubleInfoCollector;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.util.Consumer;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GitHubErrorReportSubmitter extends ErrorReportSubmitter {
    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return "\uD83D\uDC1B " + Messages.message("loc.github.error.report.submitter.report.action.text");
    }

    @Override
    public boolean submit(
            IdeaLoggingEvent @NotNull [] events,
            @Nullable String additionalInfo,
            @NotNull Component parentComponent,
            @NotNull Consumer<? super SubmittedReportInfo> consumer
    ) {
        try {
            final IdeaLoggingEvent event = events.length > 0 ? events[0] : null;


            // contains the simple error message - "Error message" field
            final String simpleErrorMessage = event != null ? event.getMessage() : Messages.message("loc.github.error.report.submitter.no.events");

            // contains the stacktrace that was thrown
            final String stackTrace = event != null ? event.getThrowableText() : Messages.message("loc.github.error.report.submitter.no.stacktrace");

            // contains information entered by user - "Please fill in any details that may be important..." field
            final String userProvidedInformation = additionalInfo != null ? additionalInfo : Messages.message("loc.github.error.report.submitter.user.did.not.enter.any.detailed.information");

            final String title = generateIssueSummary(simpleErrorMessage, stackTrace);
            final String body = generateIssueDescription(userProvidedInformation, stackTrace);

            final String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            final String encodedBody = URLEncoder.encode(body, StandardCharsets.UTF_8);
            String url = String.join("",
                    "https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/issues/new",
                    "?labels=bug",
                    "&title=" + encodedTitle,
                    "&body=" + encodedBody
            );
            Desktop.getDesktop().browse(URI.create(url));

            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
            return true;
        } catch (IOException e) {
            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED));
            return false;
        }
    }

    @NonNls
    private static final String GITHUB_ISSUE_SUMMARY_FORMAT = "[\uD83D\uDC1B bug][%s %s] %s";

    @NotNull
    private String generateIssueSummary(@Nullable final String simpleErrorMessage, @NotNull final String stackTrace) {
        final String errorMessage = simpleErrorMessage == null || simpleErrorMessage.isEmpty() ? stackTrace.split("\n\t")[0] : simpleErrorMessage;

        final ApplicationInfoImpl appInfo = (ApplicationInfoImpl) ApplicationInfoEx.getInstanceEx();
        final ApplicationNamesInfo applicationNamesInfo = ApplicationNamesInfo.getInstance();
        return String.format(GITHUB_ISSUE_SUMMARY_FORMAT, applicationNamesInfo.getProductName(), appInfo.getFullVersion(), errorMessage);
    }

    private static String wrapGitHubCode(final String code) {
        return String.format("```\n%s\n```", code);
    }

    @Language("Markdown")
    @NonNls
    private static final String MAIN_GITHUB_DESCRIPTION_FORMAT =
            String.join("\n", List.of(
                    "# User Description\n%s\n",
                    "# Stack Trace\n" + wrapGitHubCode("%s") + "\n",
                    "# Other Information\n%s\n"
            ));


    @NotNull
    private String generateIssueDescription(@NotNull final String userDescription, @NotNull final String stackTrace) {
        return String.format(MAIN_GITHUB_DESCRIPTION_FORMAT, userDescription, stackTrace, getDefaultHelpBlock());
    }

    /**
     * Generate the default help information.
     *
     * @return String formatted for GitHub
     */
    private String getDefaultHelpBlock() {
        final String generalTroubleshootingInformation = new CompositeGeneralTroubleInfoCollector().collectInfo(getLastFocusedOrOpenedProject());
        return String.join("\n", List.of(
                String.format(wrapGitHubCode(generalTroubleshootingInformation.trim()))
        ));
    }

    /**
     * Get the last focused or opened project; this is a best-effort attempt.
     * Code pulled from {org.jetbrains.ide.RestService}
     *
     * @return Project that was last in focus or open.
     */
    @NotNull
    private static Project getLastFocusedOrOpenedProject() {
        final IdeFrame lastFocusedFrame = IdeFocusManager.getGlobalInstance().getLastFocusedFrame();
        final Project project = lastFocusedFrame == null ? null : lastFocusedFrame.getProject();
        if (project == null) {
            final ProjectManager projectManager = ProjectManager.getInstance();
            final Project[] openProjects = projectManager.getOpenProjects();
            return openProjects.length > 0 ? openProjects[0] : projectManager.getDefaultProject();
        }
        return project;
    }


}

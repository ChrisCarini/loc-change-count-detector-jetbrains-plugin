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
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GitHubErrorReportSubmitter extends ErrorReportSubmitter {
    @NonNls
    private static final String TRIMMED_STACKTRACE_MARKER = "\n\n<TRIMMED STACKTRACE>";

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return "\uD83D\uDC1B " + Messages.message("loc.github.error.report.submitter.report.action.text");
    }

    @Override
    public boolean submit(
            IdeaLoggingEvent @NotNull [] events,
            @Nullable String additionalInfo,
            @NotNull Component parentComponent,
            @SuppressWarnings("deprecation") @NotNull Consumer<? super SubmittedReportInfo> consumer
    ) {
        try {
            final IdeaLoggingEvent event = events.length > 0 ? events[0] : null;


            // contains the simple error message - "Error message" field
            final String simpleErrorMessage = event != null ? event.getMessage() : Messages.message("loc.github.error.report.submitter.no.events");

            // contains the stacktrace that was thrown
            final String stackTrace = event != null ? event.getThrowableText() : Messages.message("loc.github.error.report.submitter.no.stacktrace");

            // contains information entered by user - "Please fill in any details that may be important..." field
            final String userProvidedInformation = additionalInfo != null ? additionalInfo : Messages.message("loc.github.error.report.submitter.user.did.not.enter.any.detailed.information");

            final String title = StringUtils.abbreviate(generateIssueSummary(simpleErrorMessage, stackTrace), 120);
            final String body = generateIssueDescription(userProvidedInformation, stackTrace, 6500);

            Desktop.getDesktop().browse(URI.create(buildUrl(title, body)));

            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
            return true;
        } catch (IOException e) {
            consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED));
            return false;
        }
    }

    @NotNull
    private @NonNls String buildUrl(@NotNull final String title, @NotNull final String body) {
        return String.join("",
                "https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/issues/new", //  84 chars
                "?labels=bug",                                                                                    //  11 chars
                "&title=" + URLEncoder.encode(title, StandardCharsets.UTF_8),                                     //   7 chars + encoded title (max 120 chars)
                "&body=" + URLEncoder.encode(body, StandardCharsets.UTF_8)                                        //   6 chars + encoded body (max 6500 chars)
        );
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
    private String generateIssueDescription(@NotNull final String userDescription, @NotNull final String stackTrace, int maxLength) {
        final String defaultHelpBlock = getDefaultHelpBlock();
        final String abbreviatedStackTrace = StringUtils.abbreviate(
                stackTrace,
                TRIMMED_STACKTRACE_MARKER,
                maxLength - defaultHelpBlock.length() - userDescription.length()
        );
        return String.format(MAIN_GITHUB_DESCRIPTION_FORMAT, userDescription, abbreviatedStackTrace, defaultHelpBlock);
    }

    /**
     * Generate the default help information.
     *
     * @return String formatted for GitHub
     */
    private String getDefaultHelpBlock() {
        final String generalTroubleshootingInformation = new CompositeGeneralTroubleInfoCollector().collectInfo(getLastFocusedOrOpenedProject());

        final String trimmedGeneralTroubleshootingInformation = (
                Stream.of(generalTroubleshootingInformation.split("\n"))
                        .takeWhile((@NonNls String s) -> !"=== System ===".equals(s)) // Take everything before the "System" section; we want it.
                        .filter((@NonNls String s) -> !s.startsWith("idea."))         // Filter out the `idea.<>.path` lines, as we don't want them
                        .collect(Collectors.joining("\n"))
        ) + "\n" + (
                Stream.of(generalTroubleshootingInformation.split("\n"))
                        .dropWhile((@NonNls String s) -> !"=== Plugins ===".equals(s)) // Skip everything before the "Plugins" section; we grabbed that in the stream above.
                        .takeWhile((@NonNls String s) -> !"=== Project ===".equals(s)) // Skip the "Project" section; we don't want that.
                        .collect(Collectors.joining("\n"))
        );

        return wrapGitHubCode(trimmedGeneralTroubleshootingInformation.trim());
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

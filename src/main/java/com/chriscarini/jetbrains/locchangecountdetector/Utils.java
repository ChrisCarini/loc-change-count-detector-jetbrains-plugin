package com.chriscarini.jetbrains.locchangecountdetector;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public final class Utils {

    private static final String WRAP_HTML_FORMAT = "<html>%s</html>";

    private Utils() {
    }

    public static @Nls @NotNull String wrapHtml(@NotNull final String msg) {
        return String.format(WRAP_HTML_FORMAT, msg);
    }

    public static @Nls @NotNull String generateToolTipText(@NotNull final LoCService myService) {
        int files = myService.getFileCount();
        int lines = myService.getChangeCount();

        final StringJoiner sj = new StringJoiner("<br/>");

        sj.add(String.format("You have %d LoC currently in %d files", lines, files));

        sj.add(String.format("On average, it will take about %.1f biz hrs to get this change reviewed and", myService.getReviewTime(lines)));

        sj.add(String.format("%.1f biz hrs to get this change approved!!<br/>", myService.getApprovalTime(lines)));

        sj.add(String.format("%d :: Diff between local commit and previous commit!<br/>", myService.getChangeCountInCommit()));

        sj.add(String.format("%d :: Diff between staging and previous commit!<br/>", lines));

        sj.add(String.format("%s :: Files in local commit!<br/>", myService.getFileCountInCommit()));

        sj.add(String.format("%d :: Files in staging!<br/>", files));

        return sj.toString();
    }
}
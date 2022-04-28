package com.chriscarini.jetbrains.locchangecountdetector;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public final class Utils {

    private Utils() {
    }

    public static @Nls @NotNull String generateToolTipText(@NotNull final LoCService myService) {
        int files = myService.getFileCount();
        int lines = myService.getChangeCount();

        final StringJoiner sj = new StringJoiner("<br/>");

        sj.add(Messages.message("loc.tooltip.text.status", lines, files));

        sj.add(Messages.message("loc.tooltip.text.time.review",
                String.format("%.1f", myService.getReviewTime(lines)),
                String.format("%.1f", myService.getApprovalTime(lines))
        ));

        sj.add(Messages.message("loc.tooltip.text.diff.local.and.previous", myService.getChangeCountInCommit()));

        sj.add(Messages.message("loc.tooltip.text.diff.staging.and.previous", lines));

        sj.add(Messages.message("loc.tooltip.text.files.in.local.diff", myService.getFileCountInCommit()));

        sj.add(Messages.message("loc.tooltip.text.files.in.staging.diff", files));

        return sj.toString();
    }
}
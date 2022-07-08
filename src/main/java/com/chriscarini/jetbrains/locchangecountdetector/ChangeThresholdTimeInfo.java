package com.chriscarini.jetbrains.locchangecountdetector;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Changes lower than `threshold` have `reviewTimeBizHrs` review time and `approvalTimeBizHrs` approval time.
 */
public class ChangeThresholdTimeInfo {

    // The name of the change boundary.
    @NonNls
    private final String name;

    // The threshold for the change boundary, exclusive.
    private final int threshold;

    // How long, in business hours, it takes for a code change less than `threshold` LoC to be reviewed.
    private final double reviewTimeBizHrs;

    // How long, in business hours, it takes for a code change less than `threshold` LoC to be approved.
    private final double approvalTimeBizHrs;

    public ChangeThresholdTimeInfo(@NotNull final @NonNls String name, int threshold, double reviewTimeBizHrs, double approvalTimeBizHrs) {
        this.name = name;
        this.threshold = threshold;
        this.reviewTimeBizHrs = reviewTimeBizHrs;
        this.approvalTimeBizHrs = approvalTimeBizHrs;
    }

    public String getName() {
        return name;
    }

    public int getThreshold() {
        return threshold;
    }

    public double getReviewTimeBizHrs() {
        return reviewTimeBizHrs;
    }

    public double getApprovalTimeBizHrs() {
        return approvalTimeBizHrs;
    }
}
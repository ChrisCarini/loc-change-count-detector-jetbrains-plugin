package com.chriscarini.jetbrains.locchangecountdetector.data;

/**
 * Wrapper class to contain the thresholds for displaying icons for 'error', 'warn', and 'info'.
 */
public class ChangeThresholdIconInfo {
    private final int errorAbove;
    private final int warnAbove;
    private final int infoAbove;

    public ChangeThresholdIconInfo(int errorAbove, int warnAbove, int infoAbove) {
        this.errorAbove = errorAbove;
        this.warnAbove = warnAbove;
        this.infoAbove = infoAbove;
    }

    public boolean isError(int loc) {
        return loc >= errorAbove;
    }

    public boolean isWarn(int loc) {
        return loc >= warnAbove;
    }

    public boolean isInfo(int loc) {
        return loc >= infoAbove;
    }
}
package com.chriscarini.jetbrains.locchangecountdetector;

import com.chriscarini.jetbrains.locchangecountdetector.data.ChangeInfo;
import com.chriscarini.jetbrains.locchangecountdetector.data.ChangeThresholdIconInfo;
import com.chriscarini.jetbrains.locchangecountdetector.data.ChangeThresholdTimeInfo;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ChangeThresholdService implements Disposable {
    private static final @NonNls Logger LOG = Logger.getInstance(ChangeThresholdService.class);

    private static final List<ChangeThresholdTimeInfo> DEFAULT_CHANGE_THRESHOLD_INFO = new ArrayList<>(List.of(
            new ChangeThresholdTimeInfo("XS", 10, 6, 8),
            new ChangeThresholdTimeInfo("S", 30, 6.5, 10),
            new ChangeThresholdTimeInfo("M", 100, 7.5, 15),
            new ChangeThresholdTimeInfo("L", 500, 10, 25),
            new ChangeThresholdTimeInfo("XL", 1000, 12.5, 30),
            new ChangeThresholdTimeInfo("XXL", Integer.MAX_VALUE, 15, 35)
    ));

    private static final ChangeThresholdIconInfo DEFAULT_CHANGE_THRESHOLD_ICON_INFO = new ChangeThresholdIconInfo(500, 450, 400);

    private final Project project;

    private List<ChangeThresholdTimeInfo> changeThresholdTimeInfos;
    private ChangeThresholdIconInfo changeThresholdIconInfo;

    @Nullable
    private Consumer<ChangeInfo> notificationCallback;
    @Nullable
    private Consumer<ChangeInfo> createCommitActionCallback;

    public static ChangeThresholdService getInstance(@NotNull final Project project) {
        return project.getService(ChangeThresholdService.class);
    }

    @SuppressWarnings("this-escape")
    public ChangeThresholdService(@NotNull final Project project) {
        this.project = project;
        changeThresholdTimeInfos(DEFAULT_CHANGE_THRESHOLD_INFO);
        changeThresholdIconInfo(DEFAULT_CHANGE_THRESHOLD_ICON_INFO);
        notificationCallback = null;
        createCommitActionCallback = null;
    }


    // Unused in *this* plugin. For use by consuming plugins that wish to chain methods in this service.
    @SuppressWarnings("UnusedReturnValue")
    public ChangeThresholdService changeThresholdTimeInfos(@NotNull final List<ChangeThresholdTimeInfo> changeThresholdTimeInfos) {
        LOG.debug("Setting Change Thresholds for %s", project.getName());
        if (changeThresholdTimeInfos.isEmpty()) {
            LOG.warn("Change Thresholds must have at least one entry (has %s). Using default thresholds.");
            changeThresholdTimeInfos(DEFAULT_CHANGE_THRESHOLD_INFO);
            return this;
        }
        this.changeThresholdTimeInfos = changeThresholdTimeInfos;
        this.changeThresholdTimeInfos.sort(Comparator.comparingInt(ChangeThresholdTimeInfo::getThreshold));
        return this;
    }

    // Unused in *this* plugin. For use by consuming plugins that wish to chain methods in this service.
    @SuppressWarnings("UnusedReturnValue")
    public ChangeThresholdService changeThresholdIconInfo(@NotNull final ChangeThresholdIconInfo changeThresholdIconInfo) {
        LOG.debug("Setting Change Threshold Icon Info for %s", project.getName());
        this.changeThresholdIconInfo = changeThresholdIconInfo;
        return this;
    }

    @NotNull
    public Double getReviewTime(int loc) {
        return findMatchingBoundary(loc).getReviewTimeBizHrs();
    }

    @NotNull
    public Double getApprovalTime(int loc) {
        return findMatchingBoundary(loc).getApprovalTimeBizHrs();
    }

    @NotNull
    private ChangeThresholdTimeInfo findMatchingBoundary(int loc) {
        final ChangeThresholdTimeInfo changeThresholdTimeInfo = this.changeThresholdTimeInfos.stream()
                .filter(cbi -> loc < cbi.getThreshold())
                .findFirst()
                .orElseGet(() -> {
                    final ChangeThresholdTimeInfo lastChangeThresholdTimeInfo = this.changeThresholdTimeInfos.get(this.changeThresholdTimeInfos.size() - 1);
                    LOG.debug("Unable to find change boundary less than the max threshold (%s).", lastChangeThresholdTimeInfo.getThreshold());
                    return lastChangeThresholdTimeInfo;
                });
        LOG.debug("Using change boundary %s (threshold: %s) for %s LoC.",
                changeThresholdTimeInfo.getName(),
                changeThresholdTimeInfo.getThreshold(),
                loc
        );
        return changeThresholdTimeInfo;
    }

    public List<ChangeThresholdTimeInfo> getChangeThresholdTimeInfos() {
        return changeThresholdTimeInfos;
    }

    public ChangeThresholdIconInfo getChangeThresholdIconInfo() {
        return this.changeThresholdIconInfo;
    }

    // Unused in *this* plugin. For use by consuming plugins that wish to set a callback function.
    @SuppressWarnings("unused")
    public ChangeThresholdService withNotificationCallback(@Nullable final Consumer<ChangeInfo> notificationCallback) {
        this.notificationCallback = notificationCallback;
        return this;
    }

    // Unused in *this* plugin. For use by consuming plugins that wish to set a callback function.
    @SuppressWarnings("unused")
    public ChangeThresholdService withCreateCommitActionCallback(@Nullable final Consumer<ChangeInfo> createCommitActionCallback) {
        this.createCommitActionCallback = createCommitActionCallback;
        return this;
    }

    @Nullable
    public Consumer<ChangeInfo> getNotificationCallback() {
        return notificationCallback;
    }

    @Nullable
    public Consumer<ChangeInfo> getCreateCommitActionCallback() {
        return createCommitActionCallback;
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }
}
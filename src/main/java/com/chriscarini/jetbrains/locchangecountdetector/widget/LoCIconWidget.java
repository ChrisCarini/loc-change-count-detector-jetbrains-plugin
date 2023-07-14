package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.ChangeThresholdService;
import com.chriscarini.jetbrains.locchangecountdetector.LoCCOPIcons;
import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.data.ChangeThresholdIconInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LoCIconWidget extends LoCBaseWidget implements StatusBarWidget.IconPresentation {

    public static final String ID = "LoCIcon";

    public LoCIconWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @Nullable Icon getIcon() {
        final int changeCount = LoCService.getInstance(getProject()).getChangeInfo().getLoc();

        final ChangeThresholdIconInfo thresholdIconInfo = ChangeThresholdService.getInstance(getProject()).getChangeThresholdIconInfo();

        if (thresholdIconInfo.isError(changeCount)) {
            return LoCCOPIcons.LoCCOP_Error;
        } else if (thresholdIconInfo.isWarn(changeCount)) {
            return LoCCOPIcons.LoCCOP_Warning;
        } else if (thresholdIconInfo.isInfo(changeCount)) {
            return LoCCOPIcons.LoCCOP_Info;
        }
        return LoCCOPIcons.LoCCOP;
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }
}

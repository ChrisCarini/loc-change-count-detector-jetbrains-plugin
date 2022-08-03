package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.ChangeThresholdService;
import com.chriscarini.jetbrains.locchangecountdetector.LoCCOPIcons;
import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.Utils;
import com.chriscarini.jetbrains.locchangecountdetector.data.ChangeThresholdIconInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class LoCIconWidget extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {

    public static final String ID = "LoCIcon";

    public LoCIconWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @Nullable Icon getIcon() {
        final int changeCount = LoCService.getInstance(myProject).getChangeInfo().getLoc();

        final ChangeThresholdIconInfo thresholdIconInfo = ChangeThresholdService.getInstance(myProject).getChangeThresholdIconInfo();

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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return Utils.generateToolTipText(LoCService.getInstance(myProject));
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }
}

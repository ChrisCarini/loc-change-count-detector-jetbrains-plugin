package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.LoCCOPIcons;
import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.Utils;
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

public class LOCWidgetIcon extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.IconPresentation {

    public static final String ID = "LoCIcon";

    private final LoCService locService;

    public LOCWidgetIcon(@NotNull Project project) {
        super(project);
        locService = LoCService.getInstance(this.myProject);
    }

    @Override
    public @Nullable Icon getIcon() {
        final int changeCount = locService.getChangeCount();

        if (changeCount >= 500) {
            return LoCCOPIcons.LoCCOP_Error;
        } else if (changeCount >= 450) {
            return LoCCOPIcons.LoCCOP_Warning;
        } else if (changeCount >= 400) {
            return LoCCOPIcons.LoCCOP_Info;
        }
        return LoCCOPIcons.LoCCOP;
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return Utils.generateToolTipText(locService);
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
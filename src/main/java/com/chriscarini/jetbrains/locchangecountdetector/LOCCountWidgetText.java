package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

// 1: Point of action: background job?
// 2: Analysis on change size Vs review time.
// 3: Can we show loc and review time while a user is coding?
// 4: Change Path
// 5: MP info
// 6: is it possible to see reviewTime as you're coding? or when does that pop up come up
// 7: Change the review time to CRL
// 8: Add tooltip to the status bar showing the comment for review time.

public class LOCCountWidgetText implements StatusBarWidget, StatusBarWidget.TextPresentation {

    public static final String ID = "LoCCounter";
    private final Project project;

    public LOCCountWidgetText(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public @NotNull @NlsContexts.Label String getText() {
        LoCService myService = LoCService.getInstance(project);
        return String.format("%d lines of code changed; %s files changed", myService.getChangeCount(), myService.getFileCount());
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return null;
    }


    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }
}

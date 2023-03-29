package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

public class LoCTextWidget extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

    public static final String ID = "LoCCounter";

    public LoCTextWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull @NlsContexts.Label String getText() {
        return this.getChangeText();
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return Utils.generateToolTipText(LoCService.getInstance(getProject()));
    }

    @NotNull
    private String getChangeText() {
        final LoCService service = LoCService.getInstance(getProject());
        return String.format(
                "%d/%d::%s/%s",
                service.getChangeInfo().getLocInCommit(),
                service.getChangeInfo().getLoc(),
                service.getChangeInfo().getFilesInCommit(),
                service.getChangeInfo().getFiles()
        );
    }
}

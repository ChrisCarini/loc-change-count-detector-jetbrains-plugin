package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.Utils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

public abstract class LoCBaseWidget extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.WidgetPresentation {

    public LoCBaseWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return Utils.generateToolTipText(LoCService.getInstance(getProject()));
    }

    @SuppressWarnings("UsagesOfObsoleteApi")
    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        // Return an empty consumer; if `null` (default), the tooltip will not show.
        // Maybe try removing after 2023.3 release; see https://youtrack.jetbrains.com/issue/IDEA-319569/Status-bar-provided-by-StatusBarProvider-disappears-sometimes-when-the-new-UI-is-enabled-Tabnine
        return mouseEvent -> {
        };
    }
}

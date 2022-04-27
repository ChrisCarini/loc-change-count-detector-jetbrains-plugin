package com.chriscarini.jetbrains.locchangecountdetector.factory;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

interface LOCBaseWidgetFactory extends StatusBarWidgetFactory {

    @Override
    default @Nls @NotNull String getDisplayName() {
        return Messages.message("loc.base.widget.factory.display.name");
    }

    @Override
    default boolean isAvailable(@NotNull Project project) {
        // TODO(ChrisCarini) - We could consider things like:
        //      1) Providing user a setting to show/hide icon (in general)
        //      2) Show/hide based on number of LoC changed
        return ProjectLevelVcsManager.getInstance(project).hasActiveVcss();
    }

    @Override
    default void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    default boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

    @Override
    @NonNls @NotNull String getId();

    @Override
    @NotNull StatusBarWidget createWidget(@NotNull Project project);

}


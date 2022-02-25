package com.chriscarini.jetbrains.locchangecountdetector.factory;

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
        return "Line change count plugin";
    }

    @Override
    default boolean isAvailable(@NotNull Project project) {
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


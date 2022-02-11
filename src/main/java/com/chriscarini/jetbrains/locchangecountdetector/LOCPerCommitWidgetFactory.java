package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LOCPerCommitWidgetFactory implements StatusBarWidgetFactory {

    @Override
    public @NonNls @NotNull String getId() {
        return LOCCountWidgetText.ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Line change count plugin";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        //return ProjectLevelVcsManager.getInstance(project).hasActiveVcss();
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new LOCCountWidgetText(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

}


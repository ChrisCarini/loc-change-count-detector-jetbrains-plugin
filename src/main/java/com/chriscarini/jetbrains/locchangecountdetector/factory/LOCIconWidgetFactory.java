package com.chriscarini.jetbrains.locchangecountdetector.factory;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.chriscarini.jetbrains.locchangecountdetector.widget.LoCIconWidget;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LOCIconWidgetFactory implements LOCBaseWidgetFactory {

    public @NotNull String getDisplayName() {
        return Messages.message("loc.widget.factory.icon.display.name");
    }

    @Override
    public @NonNls @NotNull String getId() {
        return LoCIconWidget.ID;
    }

    @Override
    public @NotNull StatusBarWidget getWidget(@NotNull Project project) {
        return new LoCIconWidget(project);
    }
}


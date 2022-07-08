package com.chriscarini.jetbrains.locchangecountdetector.factory;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.chriscarini.jetbrains.locchangecountdetector.widget.LoCTextWidget;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LoCTextWidgetFactory implements LOCBaseWidgetFactory {

    public @NotNull String getDisplayName() {
        return Messages.message("loc.widget.factory.text.display.name");
    }

    @Override
    public @NonNls @NotNull String getId() {
        return LoCTextWidget.ID;
    }

    @Override
    public @NotNull StatusBarWidget getWidget(@NotNull Project project) {
        return new LoCTextWidget(project);
    }
}


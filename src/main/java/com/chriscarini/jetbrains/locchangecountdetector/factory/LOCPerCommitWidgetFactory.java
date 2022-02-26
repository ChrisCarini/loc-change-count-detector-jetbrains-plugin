package com.chriscarini.jetbrains.locchangecountdetector.factory;

import com.chriscarini.jetbrains.locchangecountdetector.widget.LOCCountWidgetText;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LOCPerCommitWidgetFactory implements LOCBaseWidgetFactory {

    @Override
    public @NonNls @NotNull String getId() {
        return LOCCountWidgetText.ID;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new LOCCountWidgetText(project);
    }

}


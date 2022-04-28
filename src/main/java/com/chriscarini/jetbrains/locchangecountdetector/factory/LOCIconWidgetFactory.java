package com.chriscarini.jetbrains.locchangecountdetector.factory;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.chriscarini.jetbrains.locchangecountdetector.widget.LOCWidgetIcon;
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
        return LOCWidgetIcon.ID;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new LOCWidgetIcon(project);
    }

//    @Override
//    public boolean isAvailable(@NotNull Project project) {
//        // TODO(ChrisCarini) - We could consider showing the icon only if the change size exceeds a certain limit.
//        return ProjectLevelVcsManager.getInstance(project).hasActiveVcss() && LoCService.getInstance(project).getChangeCount() >= 300;
//    }

}


package com.chriscarini.jetbrains.locchangecountdetector.factory;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public interface LOCBaseWidgetFactory extends StatusBarWidgetFactory {
    Set<String> widgetIds = new HashSet<>();

    static void registerId(final String id) {
        widgetIds.add(id);
    }

    static void updateAllWidgets(@NotNull final Project project) {
        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            widgetIds.forEach(statusBar::updateWidget);
        }
    }

    @Override
    default
    @Nls @NotNull String getDisplayName() {
        return Messages.message("loc.base.widget.factory.display.name");
    }

    @Override
    default boolean isAvailable(@NotNull Project project) {
        // TODO(ChrisCarini) - We could consider things like:
        //      1) Providing user a setting to show/hide icon (in general)
        //      2) Show/hide based on number of LoC changed
        // Note: We avoid using `ProjectLevelVcsManager.getInstance(project).hasActiveVcss()` since this will cause the
        // widgets to *not* show upon startup as VCS may not yet be 'ready' yet according to IDE. To account for that,
        // we simply assume this widget is always available.
        return true;
    }

    @Override
    @NonNls @NotNull String getId();

    @Override
    @NotNull
    default StatusBarWidget createWidget(@NotNull Project project) {
        registerId(getId());
        return getWidget(project);
    }

    @NotNull StatusBarWidget getWidget(@NotNull Project project);

}


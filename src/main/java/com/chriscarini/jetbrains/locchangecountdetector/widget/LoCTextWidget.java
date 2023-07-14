package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LoCTextWidget extends LoCBaseWidget implements StatusBarWidget.TextPresentation {

    public static final String ID = "LoCCounter";

    public LoCTextWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public @NotNull @NlsContexts.Label String getText() {
        return this.getChangeText();
    }

    @Override
    public float getAlignment() {
        return 0;
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

package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.icons.AllIcons;
import com.intellij.ide.PowerSaveMode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class VCSFileChangeCountWidgetFactory implements StatusBarWidgetFactory {
    private static final String ID = "VCSFileChangeCount";

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Nls
    @Override
    public @NotNull String getDisplayName() {
        return "VCS File Change Count";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            ApplicationManager.getApplication().getMessageBus().connect().subscribe(PowerSaveMode.TOPIC, () -> statusBar.updateWidget(getId()));
        }
//        return new VCSFileChangeCountWidgetIcon(project);
        return new VCSFileChangeCountWidgetText();
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    private static class VCSFileChangeCountWidgetText implements StatusBarWidget, StatusBarWidget.TextPresentation {
        @Override
        public @NotNull @NlsContexts.Label String getText() {
            return String.format("%s Files Changed; %s Lines Changed - Hi Sulabh!", 123, 456);
        }

        @Override
        public float getAlignment() {
            return 0;
        }

        @Override
        public @NonNls @NotNull String ID() {
            return "NOT NULL.";
        }

        @Override
        public void install(@NotNull StatusBar statusBar) {
        }

        @Override
        public void dispose() {

        }

        @Override
        public @Nullable @NlsContexts.Tooltip String getTooltipText() {
            return null;
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return null;
        }
    }

    private static class VCSFileChangeCountWidgetIcon implements StatusBarWidget, StatusBarWidget.IconPresentation {
        private final Project project;

        public VCSFileChangeCountWidgetIcon(@NotNull Project project) {
            this.project = project;
        }

        @Override
        public @NotNull String ID() {
            return ID;
        }


        @Override
        public void install(@NotNull StatusBar statusBar) {
        }

        @Override
        public @Nullable WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @Nullable String getTooltipText() {
            final AtomicInteger fileCount = new AtomicInteger();
            final AtomicInteger lineCount = new AtomicInteger();

            /*
            The code below was my attempt to find the right SDK API to get LoC changes, but i couldn't figure it out.
             */

            ChangeListManager clm = ChangeListManager.getInstance(this.project);
            clm.getAllChanges().parallelStream().forEach(change -> {
                fileCount.getAndIncrement();

                final VirtualFile virtualFile = Objects.requireNonNull(change.getVirtualFile());

//                final PsiElement firstElement = PsiManager.getInstance(project).findFile(virtualFile).getFirstChild();

                final int changedFileLineCount = LineUtil.countLines(PsiManager.getInstance(project).findFile(virtualFile));

                lineCount.getAndAdd(changedFileLineCount);

                final String fileContents = Objects.requireNonNull(PsiManager.getInstance(project).findFile(virtualFile)).getText();
            });

            // you can kinda see what i was going for here tho..
            return String.format("%s Files Changed; %s Lines Changed - Hi Sulabh!", fileCount.get(), lineCount.get());
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return __ -> PowerSaveMode.setEnabled(!PowerSaveMode.isEnabled());
        }

        @Override
        public @Nullable Icon getIcon() {
            return PowerSaveMode.isEnabled() ? AllIcons.General.BalloonInformation : AllIcons.General.BalloonWarning;
        }

        @Override
        public void dispose() {
        }
    }
}

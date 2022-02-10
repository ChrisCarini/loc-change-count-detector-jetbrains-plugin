package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.List;

public class LOCCountWidgetText extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

    public static final String ID = "LoCCounter";
    public static final String NOTIFICATION_GROUP = "LoCCOPNotification";
    private MergingUpdateQueue myQueue;
    @SuppressWarnings("UnstableApiUsage")
    private @NlsContexts.Label String myText;

    private final LoCService locService;

    protected LOCCountWidgetText(@NotNull Project project) {
        super(project);
        locService = LoCService.getInstance(myProject);

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                boolean fileWasSaved = false;

                // Iterate over all the files and check if any are from save events
                for (VFileEvent event : events) {
                    if (event.isFromSave()) {
                        fileWasSaved = true;
                    }
                }

                // If any files changed, schedule an update
                if (fileWasSaved) {
                    ApplicationManager.getApplication().invokeLater(() -> updateChangeText());
                }
            }
        });
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        this.myQueue = new MergingUpdateQueue("PositionPanel", 100, true, null, this);
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull @NlsContexts.Label String getText() {
        return this.myText == null ? "" : this.myText;
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        int files = Integer.parseInt(locService.getFileCount());
        int lines = locService.getChangeCount();

        return "You have " + lines + " LoC currently in " + files + " files." +
                "<br/>" +
                " On average, it will take about " +
                locService.getReviewTime(lines) +
                " biz hrs to get this change reviewed and "
                + "<br/>" +
                locService.getApprovalTime(lines) +
                " biz hrs to get this change approved!!"
                + "<br/>"
                + "<br/>"
                + locService.getChangeCountInCommit() + ":: Diff between local commit and previous commit!"
                + "<br/>"
                + "<br/>"
                + lines + ":: Diff between staging and previous commit!"
                + "<br/>"
                + "<br/>"
                + locService.getFileCountInCommit() + ":: Files in local commit!"
                + "<br/>"
                + "<br/>"
                + files + ":: Files in staging!"
                ;
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    }

    private void updateChangeText() {
        locService.computeLoCInfo();
        myQueue.queue(Update.create(this, () -> {
            String newText = this.getChangeText();
            if (newText.equals(myText)) return;

            myText = newText;
            if (myStatusBar != null) {
                myStatusBar.updateWidget(ID());

                Integer changeCount = locService.getChangeCount();
                if (changeCount > 500) {

                    final Notification notification = new Notification(NOTIFICATION_GROUP, "Large change detected",
                            String.format("You have made a change that is %s lines of code.<br/>Consider creating a PR.", changeCount), NotificationType.INFORMATION);
                    notification.setIcon(AllIcons.General.Warning);
                    notification.addAction(new AnAction() {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            final Notification notification = new Notification(NOTIFICATION_GROUP, "Clicked the action",
                                    "You just clicked the action!", NotificationType.INFORMATION);
                            notification.notify(myProject);
                        }
                    });
                    notification.notify(myProject);
                }
            }
        }));
    }

    @NotNull
    private String getChangeText() {
        return String.format("%d/%d::%s/%s", locService.getChangeCountInCommit(), locService.getChangeCount(), locService.getFileCountInCommit(), locService.getFileCount());
    }

}
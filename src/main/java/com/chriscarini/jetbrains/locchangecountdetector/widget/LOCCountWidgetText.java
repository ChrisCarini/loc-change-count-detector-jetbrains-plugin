package com.chriscarini.jetbrains.locchangecountdetector.widget;

import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.Utils;
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
import com.intellij.openapi.vcs.actions.CommonCheckinProjectAction;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.chriscarini.jetbrains.locchangecountdetector.LoCCOPIcons;
import com.chriscarini.jetbrains.locchangecountdetector.LoCService;
import com.chriscarini.jetbrains.locchangecountdetector.Utils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
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
    private Notification existingNotification;

    public LOCCountWidgetText(@NotNull Project project) {
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
        return Utils.generateToolTipText(locService);
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
                myStatusBar.updateWidget(LOCWidgetIcon.ID);

                Integer changeCount = locService.getChangeCount();
                if (changeCount >= 500) {

                    // Clear any existing notification, the number is likely different.
                    clearExistingNotification();

                    // TODO(ChrisCarini) - found here: https://www.plugin-dev.com/intellij/general/notifications/#classes
                    //  Clean this up and see if we can use `NotificationGroupManager` to create this class.
                    final class MyNotification extends Notification implements NotificationFullContent {
                        private MyNotification(@NlsContexts.NotificationTitle String title, @NlsContexts.NotificationContent String content, NotificationType type) {
                            super(NOTIFICATION_GROUP, title, content, type);
                        }
                    }

                    final Notification notification = new MyNotification("Large change detected!",
                            Utils.wrapHtml(String.format("You have made a change that is %s lines of code.<br/>Consider creating a PR.", changeCount)), NotificationType.INFORMATION);
                    notification.setIcon(LoCCOPIcons.LoCCOP_Warning);
                    notification.addAction(new CreateCommitAction(myProject));
                    notification.notify(myProject);
                    existingNotification = notification;

//                    NotificationGroupManager.getInstance()
//                            .getNotificationGroup(NOTIFICATION_GROUP_ID)
//                            .createNotification(
//                                    "Large change detected!",
//                                    wrapHtml(String.format("You have made a change that is %s lines\n<br/>of code. Consider creating a PR.", changeCount)),
//                                    NotificationType.INFORMATION
//                            )
//                            .setIcon(LoCCOPIcons.LoCCOP_Warning)
//                            .addAction(new CreateCommitAction(myProject))
//                            .notify(myProject);
                } else {
                    // Clear any existing notification, the number is likely different.
                    clearExistingNotification();
                }
            }
        }));
    }

    private void clearExistingNotification() {
        if (existingNotification != null) {
            existingNotification.expire();
            existingNotification = null;
        }
    }

    private static class CreateCommitAction extends AnAction implements DumbAware {
        private final Project myProject;

        public CreateCommitAction(@NotNull final Project project) {
            super("Create Commit Now!");
            this.myProject = project;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // Pull up the commit dialog...
            final CommonCheckinProjectAction f = new CommonCheckinProjectAction();
            //noinspection UnstableApiUsage
            f.actionPerformed(e);


            // Give a final notification
            NotificationGroupManager.getInstance()
                    .getNotificationGroup(NOTIFICATION_GROUP)
                    .createNotification(
                            "Clicked the action",
                            "Hey, thank you for committing early and often!",
                            NotificationType.INFORMATION
                    )
                    .setIcon(LoCCOPIcons.LoCCOP_OK)
                    .notify(myProject);
        }
    }

    @NotNull
    private String getChangeText() {
        return String.format("%d/%d::%s/%s", locService.getChangeCountInCommit(), locService.getChangeCount(), locService.getFileCountInCommit(), locService.getFileCount());
    }


}

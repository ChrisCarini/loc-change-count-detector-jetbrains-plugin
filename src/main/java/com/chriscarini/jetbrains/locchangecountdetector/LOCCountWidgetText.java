package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
    LoCService myService;
    private MergingUpdateQueue myQueue;
    private @NlsContexts.Label String myText;
    private Project project;

    protected LOCCountWidgetText(@NotNull Project project) {
        super(project);
        this.project = project;
        myService = LoCService.getInstance(this.myProject);

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).isFromSave()) {
                        updateChangeText();
                    }
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

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        int files = Integer.parseInt(myService.getFileCount());
        int lines = myService.getChangeCount();

        return "You have " + lines + " LoC currently in " + files + " files." +
                "<br/>" +
                " On average, it will take about " +
                myService.getReviewTime(lines) +
                " biz hrs to get this change reviewed and "
                + "<br/>" +
                myService.getApprovalTime(lines) +
                " biz hrs to get this change approved!!"
                + "<br/"
                + "<br/"
                + myService.getChangeCountInCommit() + ":: Diff between local commit and previous commit!"
                + "<br/"
                + "<br/"
                + lines + ":: Diff between staging and previous commit!"
                + "<br/"
                + "<br/"
                + myService.getFileCountInCommit() + ":: Files in local commit!"
                + "<br/"
                + "<br/"
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
        LoCService.getInstance(this.project).computeLoCInfo();
        myQueue.queue(Update.create(this, () -> {
            String newText = this.getChangeText();
            if (newText.equals(myText)) return;

            myText = newText;
            if (myStatusBar != null) {
                myStatusBar.updateWidget(ID());

                Integer changeCount = myService.getChangeCount();
                if (changeCount > 500) {

                    final Notification notification = new Notification("ProjectOpenNotification", "Large Change Detected",
                            String.format("You have made a change that is %s lines of code.<br/>Consider creating a PR.", changeCount), NotificationType.INFORMATION);
                    notification.setIcon(AllIcons.General.Warning);
                    notification.addAction(new AnAction() {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            final Notification notification = new Notification("ProjectOpenNotification", "Clicked the action",
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
        return String.format("%d/%d::%s/%s", myService.getChangeCountInCommit(), myService.getChangeCount(), myService.getFileCountInCommit(), myService.getFileCount());
    }

}
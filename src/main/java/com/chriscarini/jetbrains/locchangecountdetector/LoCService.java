package com.chriscarini.jetbrains.locchangecountdetector;

import com.chriscarini.jetbrains.locchangecountdetector.data.ChangeInfo;
import com.chriscarini.jetbrains.locchangecountdetector.factory.LOCBaseWidgetFactory;
import com.chriscarini.jetbrains.locchangecountdetector.git.GitNumStat;
import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.actions.commit.CommonCheckinProjectAction;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import git4idea.commands.GitCommand;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class LoCService implements Disposable {
    private static final @NonNls Logger LOG = Logger.getInstance(LoCService.class);

    public static final String NOTIFICATION_GROUP = "LoCCOPNotification";
    private static final GitNumStat GIT_DIFF_NUMSTAT = new GitNumStat(GitCommand.DIFF);
    private static final GitNumStat GIT_SHOW_NUMSTAT = new GitNumStat(GitCommand.SHOW);
    private ChangeInfo changeInfo = new ChangeInfo();
    private final MergingUpdateQueue myQueue;

    private Notification existingNotification;

    private final Project project;

    public static LoCService getInstance(@NotNull final Project project) {
        return project.getService(LoCService.class);
    }

    @SuppressWarnings("this-escape")
    public LoCService(@NotNull final Project project) {
        this.project = project;

        this.myQueue = new MergingUpdateQueue("PositionPanel", 100, true, null, this);

        this.computeLoCInfo();

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                // Recompute LoC information if one of the events is from the current project. Otherwise, ignore.
                for (VFileEvent event : events) {
                    if (event == null || event.getFile() == null) {
                        continue;
                    }
                    if (ProjectRootManager.getInstance(project).getFileIndex().isInContent(Objects.requireNonNull(event.getFile()))) {
                        LOG.debug(String.format("%s - Running LoC computation after VFS changes.", project.getName()));
                        computeLoCInfo();
                        return;
                    }
                }
            }
        });
    }

    private Pair<Integer, Integer> getGitShowHeadNumStat() {
        LOG.debug(String.format("%s - Running `git show --numstat`...", project.getName()));
        return GIT_SHOW_NUMSTAT.compute(project).toPair();
    }

    private Pair<Integer, Integer> getGitDiffHeadNumStat() {
        LOG.debug(String.format("%s - Running `git diff --numstat`...", project.getName()));
        return GIT_DIFF_NUMSTAT.compute(project).toPair();
    }

    public void computeLoCInfo() {
        ProgressManager.getInstance().run(new Task.Backgroundable(this.project, Messages.message("loc.service.compute.progress.backgroundable.title")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                if (myProject == null) {
                    return;
                }

                final Pair<Integer, Integer> info = getGitDiffHeadNumStat();
                final Pair<Integer, Integer> infoInHeadCommit = getGitShowHeadNumStat();

                final ChangeInfo changeInfo = new ChangeInfo.Builder()
                        .setModifiedStagedChanges(info)
                        .setHeadCommitChanges(infoInHeadCommit)
                        .build();

                LoCService.getInstance(myProject).setChangeInfo(changeInfo);

                // Queue UI updates to LoC widgets
                myQueue.queue(Update.create(LOCBaseWidgetFactory.class, () -> {
                    LOG.debug(String.format("%s - Queuing update of all LoC widgets...", project.getName()));
                    LOCBaseWidgetFactory.updateAllWidgets(project);
                }));

                // Queue large LoC notification
                myQueue.queue(Update.create(NOTIFICATION_GROUP, () -> {
                    LOG.debug(String.format("%s - Queuing large LoC notification...", project.getName()));
                    triggerNotification();
                }));
            }
        });
    }

    private void triggerNotification() {
        // Clear any existing notification, the number is likely different.
        clearExistingNotification();

        final Integer changeCount = this.getChangeInfo().getLoc();

        // TODO(ccarini) - Perhaps expose the `500` limit as a configuration option.
        // If the LoC change is less than 500, we have no need/desire for a notification, skip...
        if (changeCount < 500) {
            return;
        }

        final Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP)
                .createNotification(
                        Messages.message("loc.count.widget.text.update.change.text.notification.title"),
                        Messages.message("loc.count.widget.text.update.change.text.notification.content", changeCount),
                        NotificationType.INFORMATION
                )
                .setIcon(LoCCOPIcons.LoCCOP_Warning)
                .addAction(new CreateCommitAction(project));
        notification.notify(project);
        existingNotification = notification;

        final Consumer<ChangeInfo> notificationCallback = ChangeThresholdService.getInstance(project).getNotificationCallback();
        if (notificationCallback != null) {
            runCallback(notificationCallback);
        }
    }

    private void runCallback(@NotNull final Consumer<ChangeInfo> callback) {
        final LoCService service = LoCService.getInstance(project);
        callback.accept(service.changeInfo);
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
            super(Messages.message("loc.count.widget.text.create.commit.action.text"));
            this.myProject = project;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // Pull up the commit dialog...
            final CommonCheckinProjectAction f = new CommonCheckinProjectAction();
            f.actionPerformed(e);

            final Consumer<ChangeInfo> callback = ChangeThresholdService.getInstance(myProject).getCreateCommitActionCallback();
            if (callback != null) {
                LoCService.getInstance(myProject).runCallback(callback);
            }

            // Give a final notification
            NotificationGroupManager.getInstance()
                    .getNotificationGroup(NOTIFICATION_GROUP)
                    .createNotification(
                            Messages.message("loc.count.widget.text.create.commit.action.notification.title"),
                            Messages.message("loc.count.widget.text.create.commit.action.notification.content"),
                            NotificationType.INFORMATION
                    )
                    .setIcon(LoCCOPIcons.LoCCOP_OK)
                    .notify(myProject);
        }
    }

    @NotNull
    public ChangeInfo getChangeInfo() {
        return changeInfo;
    }

    public void setChangeInfo(ChangeInfo changeInfo) {
        this.changeInfo = changeInfo;
    }

    @NotNull
    public Double getReviewTime(int loc) {
        return ChangeThresholdService.getInstance(project).getReviewTime(loc);
    }

    @NotNull
    public Double getApprovalTime(int loc) {
        return ChangeThresholdService.getInstance(project).getApprovalTime(loc);
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }
}
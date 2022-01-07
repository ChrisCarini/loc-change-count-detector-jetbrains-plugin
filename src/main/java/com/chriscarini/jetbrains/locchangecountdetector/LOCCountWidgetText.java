package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LOCCountWidgetText extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.TextPresentation,
        BulkAwareDocumentListener.Simple, CaretListener, SelectionListener, PropertyChangeListener {

    public static final String ID = "LoCCounter";

    private MergingUpdateQueue myQueue;
    private @NlsContexts.Label String myText;
    LoCService myService;
    private Project project;

    protected LOCCountWidgetText(@NotNull Project project) {
        super(project);
        this.project = project;
        myService = LoCService.getInstance(this.myProject);
    }


    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        this.myQueue = new MergingUpdateQueue("PositionPanel", 100, true, null, this);
        EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addCaretListener(this, this);
        multicaster.addSelectionListener(this, this);
        multicaster.addDocumentListener(this, this);
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
                + myService.getFileCountInCommit()+ ":: Files in local commit!"
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
    public void afterDocumentChange(@NotNull Document document) {
        saveDocAndUpdate(document);
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        saveDocAndUpdate(event.getEditor().getDocument());
    }


    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        final VirtualFile file = event.getNewEditor().getFile();
        if (file != null) {
            final Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                this.saveDocAndUpdate(document);
            }
            return;
        }

        this.updateChangeText();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        this.updateChangeText();
    }

    /**
     * Save the provided document and update LoC info.
     * TODO(ChrisCarini) - This is *SUPER* sub-ideal, and only being committed for the hack-demo tomorrow. Figure out a better way.
     *
     * @param document The document to save.
     */
    private void saveDocAndUpdate(@NotNull Document document) {
        FileDocumentManager.getInstance().saveDocument(document);
        LoCService.getInstance(this.project).computeLoCInfo();
        this.updateChangeText();
    }


    private void updateChangeText() {
//        this.myText = this.getChangeText();
//        myStatusBar.updateWidget(ID());
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

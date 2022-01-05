package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
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

// 1: Point of action: background job?
// 2: Analysis on change size Vs review time.
// 3: Can we show loc and review time while a user is coding?
// 4: Change Path
// 5: MP info
// 6: is it possible to see reviewTime as you're coding? or when does that pop up come up
// 7: Change the review time to CRL
// 8: Add tooltip to the status bar showing the comment for review time.

public class LOCCountWidgetText extends EditorBasedWidget implements StatusBarWidget, StatusBarWidget.TextPresentation,
        BulkAwareDocumentListener.Simple, CaretListener, SelectionListener {

    public static final String ID = "LoCCounter";

    private MergingUpdateQueue myQueue;
    private @NlsContexts.Label String myText;

    protected LOCCountWidgetText(@NotNull Project project) {
        super(project);
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
        return null;
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void afterDocumentChange(@NotNull Document document) {
        this.updateChangeText();
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        this.updateChangeText();
    }


    private void updateChangeText() {
        myQueue.queue(Update.create(this, () -> {
            String newText = this.getChangeText();
            if (newText.equals(myText)) return;

            myText = newText;
            if (myStatusBar != null) {
                myStatusBar.updateWidget(ID());
            }
        }));
    }

    @NotNull
    private String getChangeText() {
        final LoCService myService = LoCService.getInstance(this.myProject);
        return String.format("%d lines of code changed; %s files changed", myService.getChangeCount(), myService.getFileCount());
    }
}

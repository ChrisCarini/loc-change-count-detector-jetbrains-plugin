package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.openapi.vcs.changes.ChangeListDecorator;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

public class VCSFileChangeCounter implements ChangeListDecorator {
    @Override
    public void decorateChangeList(@NotNull LocalChangeList changeList, @NotNull ColoredTreeCellRenderer cellRenderer, boolean selected, boolean expanded, boolean hasFocus) {

    }
}

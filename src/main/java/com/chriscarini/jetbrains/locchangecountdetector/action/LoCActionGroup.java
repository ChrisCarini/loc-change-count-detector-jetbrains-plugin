package com.chriscarini.jetbrains.locchangecountdetector.action;

import com.chriscarini.jetbrains.locchangecountdetector.LoCCOPIcons;
import com.intellij.openapi.actionSystem.DefaultActionGroup;


public class LoCActionGroup extends DefaultActionGroup {
    public LoCActionGroup() {
        super();
        getTemplatePresentation().setText("LoC COP"); //NON-NLS - This is for developer debugging only.
        getTemplatePresentation().setIcon(LoCCOPIcons.LoCCOP);
    }
}

package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;


/**
 * Icons for LoC COP
 */
public final class LoCCOPIcons {
    public static final Icon LoCCOP = IconLoader.getIcon("/icons/LoCCOP.png", LoCCOPIcons.class);

    /* The LoC COP icon with a green check mark on top left corner. */
    public static final Icon LoCCOP_OK = LayeredIcon.create(LoCCOP, IconUtil.scale(AllIcons.General.InspectionsOK, null, 0.625f));

    /* The LoC COP icon with a yellow warning mark on top left corner. */
    public static final Icon LoCCOP_Warning = LayeredIcon.create(LoCCOP, IconUtil.scale(AllIcons.General.BalloonWarning, null, 0.625f));

    /* The LoC COP icon with a red error mark on top left corner. */
    public static final Icon LoCCOP_Error = LayeredIcon.create(LoCCOP, IconUtil.scale(AllIcons.General.BalloonError, null, 0.625f));

    /* The LoC COP icon with a blue info mark on top left corner. */
    public static final Icon LoCCOP_Info = LayeredIcon.create(LoCCOP, IconUtil.scale(AllIcons.General.BalloonInformation, null, 0.625f));
}

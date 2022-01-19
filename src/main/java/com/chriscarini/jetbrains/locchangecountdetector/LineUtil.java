package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public final class LineUtil {

    public static final char NEW_LINE_CHAR = '\n';
    public static final char CARRIAGE_RETURN_CHAR = '\r';
    public static final char TAB_CHAR = '\t';

    private LineUtil() {
    }

    public static int countLines(@Nullable PsiElement element) {
        if (element == null || element instanceof PsiCompiledElement) {
            return 0;
        }
        return countLines(element.getText());
    }

    static int countLines(String text) {
        if (text == null) {
            return 0;
        }
        int lines = 0;
        boolean onEmptyLine = true;
        final char[] chars = text.toCharArray();
        for (char aChar : chars) {
            if (LineUtil.isNewlineOrCarriageReturn(aChar)) {
                if (!onEmptyLine) {
                    lines++;
                    onEmptyLine = true;
                }
            } else if (aChar != ' ' && aChar != TAB_CHAR) {
                onEmptyLine = false;
            }
        }
        if (!onEmptyLine) {
            lines++;
        }
        return lines;
    }

    public static boolean isNewlineOrCarriageReturn(final char charToTest) {
        return charToTest == NEW_LINE_CHAR || charToTest == CARRIAGE_RETURN_CHAR;
    }
}
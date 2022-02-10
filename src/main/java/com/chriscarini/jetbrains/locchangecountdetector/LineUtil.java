package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public final class LineUtil {

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
            if (aChar == '\n' || aChar == '\r') {
                if (!onEmptyLine) {
                    lines++;
                    onEmptyLine = true;
                }
            } else if (aChar != ' ' && aChar != '\t') {
                onEmptyLine = false;
            }
        }
        if (!onEmptyLine) {
            lines++;
        }
        return lines;
    }
}
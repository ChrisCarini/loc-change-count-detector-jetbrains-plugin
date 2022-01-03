package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
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

    public static int countCommentOnlyLines(PsiComment comment) {
        final String text = comment.getText();
        int totalLines = countLines(text);
        boolean isOnSameLineBeforeCode = false;
        if (!endsInLineBreak(comment)) {
            PsiElement nextSibling = comment.getNextSibling();

            while (nextSibling != null) {
                if (nextSibling instanceof PsiComment || nextSibling instanceof PsiWhiteSpace) {
                    if (containsLineBreak(nextSibling)) {
                        break;
                    }
                } else {
                    isOnSameLineBeforeCode = true;
                }
                nextSibling = nextSibling.getNextSibling();
            }
        }
        boolean isOnSameLineAfterCode = false;
        PsiElement prevSibling = comment.getPrevSibling();
        while (prevSibling != null) {
            if (prevSibling instanceof PsiComment || prevSibling instanceof PsiWhiteSpace) {
                if (containsLineBreak(prevSibling)) {
                    break;
                }
            } else {
                isOnSameLineAfterCode = true;
            }
            prevSibling = prevSibling.getPrevSibling();
        }

        if (isOnSameLineAfterCode) {
            totalLines = Math.max(totalLines - 1, 0);
        }
        if (isOnSameLineBeforeCode) {
            totalLines = Math.max(totalLines - 1, 0);
        }

        return totalLines;
    }

    private static boolean endsInLineBreak(PsiElement element) {
        if (element == null) {
            return false;
        }
        final String text = element.getText();
        if (text == null) {
            return false;
        }
        final char endChar = text.charAt(text.length() - 1);
        return endChar == '\n' || endChar == '\r';
    }

    private static boolean containsLineBreak(PsiElement element) {
        if (element == null) {
            return false;
        }
        final String text = element.getText();
        return text != null && (text.contains(new String("\n")) || text.contains(new String("\r")));
    }
}
package io.github.intellij.dlanguage.annotator;

import consulo.annotation.access.RequiredReadAction;
import consulo.d.localize.DLocalize;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.psi.DLanguageLiteralExpression;

import static io.github.intellij.dlanguage.utils.DStringUtilKt.getCorrespondingClosingDelimiter;
import static io.github.intellij.dlanguage.utils.DStringUtilKt.getOpeningDelimiter;
import static io.github.intellij.dlanguage.utils.DStringUtilKt.isPredefinedDelimiter;

/**
 * Handle all errors in String and characters literals. It annotates the errors on invalid literal.
 */
public class DStringLiteralAnnotator implements Annotator {

    @Override
    @RequiredReadAction
    public void annotate(PsiElement element, AnnotationHolder holder) {
        if (!(element instanceof DLanguageLiteralExpression literalExpression)) {
            return;
        }
        if (!literalExpression.getDELIMITED_STRINGs().isEmpty()) {
            for (PsiElement elem : literalExpression.getDELIMITED_STRINGs()) {
                String text = elem.getText();
                int endPos = text.charAt(elem.getTextLength() - 1) == '"' ? 1 : 2;
                String value = text.substring(2, elem.getTextLength() - endPos); // Skip `q"` and `"`
                String openingDelimiter = getOpeningDelimiter(value);
                if (openingDelimiter == null) {
                    LocalizeValue message = Character.isWhitespace(value.charAt(0))
                        ? DLocalize.annotatorDelimitedStringWhitespaceDelimiter()
                        : DLocalize.annotatorDelimitedStringInvalidDelimiter();
                    holder.newAnnotation(HighlightSeverity.ERROR, message).create();
                    continue;
                }
                String closingDelimiter = getCorrespondingClosingDelimiter(openingDelimiter);

                // impossible, handled at lexer directly
                if (!closingDelimiter.equals(openingDelimiter)) {
                    continue;
                }

                if (!isPredefinedDelimiter(openingDelimiter)) {
                    closingDelimiter = "\n" + closingDelimiter;
                }

                int lines = StringUtil.getOccurrenceCount(value, closingDelimiter) + 1;
                int expectedDelimitersCount = isPredefinedDelimiter(openingDelimiter)
                    && getCorrespondingClosingDelimiter(openingDelimiter).equals(openingDelimiter) ? 3 : 2;
                if (lines > expectedDelimitersCount) {
                    int elemStartIndex = elem.getTextRange().getStartOffset() + 2; // + 2 to because we skipped q"
                    holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.annotatorDelimitedStringTextAfterClosingDelimiter())
                        .range(new TextRange(
                            elemStartIndex + ordinalIndexOf(value, closingDelimiter, expectedDelimitersCount - 1) + closingDelimiter.length(),
                            elemStartIndex + value.length()
                        )).create();
                }
            }
        }
    }

    /**
     * Apache Commons' {@code StringUtils.ordinalIndexOf}, which upstream uses: the index of the {@code ordinal}-th
     * occurrence of {@code searchStr}, or -1.
     */
    private static int ordinalIndexOf(String str, String searchStr, int ordinal) {
        int found = 0;
        int index = -1;
        do {
            index = str.indexOf(searchStr, index + 1);
            if (index < 0) {
                return index;
            }
            found++;
        }
        while (found < ordinal);
        return index;
    }
}

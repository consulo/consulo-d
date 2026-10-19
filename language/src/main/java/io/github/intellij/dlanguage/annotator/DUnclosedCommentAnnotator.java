package io.github.intellij.dlanguage.annotator;

import consulo.annotation.access.RequiredReadAction;
import consulo.d.localize.DLocalize;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DLanguage;

/**
 * Detect if a comment is not closed and annotate with error if that’s the case.
 */
public class DUnclosedCommentAnnotator implements Annotator {

    @Override
    @RequiredReadAction
    public void annotate(PsiElement element, AnnotationHolder holder) {
        if (!(element instanceof PsiComment) || element.getLanguage() != DLanguage.INSTANCE) {
            return;
        }
        String text = element.getText();
        if (text.startsWith("/*") && !text.endsWith("*/") ||
            text.startsWith("/+") && (!text.endsWith("+/") ||
                StringUtil.getOccurrenceCount(text, "/+") != StringUtil.getOccurrenceCount(text, "+/"))) {
            int start = element.getTextRange().getEndOffset() - 1;
            int end = start + 1;
            holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.annotatorUnclosedComment())
                .range(new TextRange(start, end)).create();
        }
    }
}

package io.github.intellij.dlanguage.sdlang.annotator;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.sdlang.colors.SDLangColor;
import io.github.intellij.dlanguage.sdlang.psi.SDLangTagId;

public class SDLangHighlightAnnotator implements Annotator {

    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        if (element instanceof SDLangTagId) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(SDLangColor.TAG_IDENTIFIER.getTextAttributesKey()).create();
        }
    }
}

package io.github.intellij.dlanguage.highlighting.annotation;

import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.psi.PsiFile;

public interface DProblem {
    void createAnnotations(PsiFile file, AnnotationHolder holder);

}

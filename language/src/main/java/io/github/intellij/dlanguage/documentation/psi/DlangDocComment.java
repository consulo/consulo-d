package io.github.intellij.dlanguage.documentation.psi;

import consulo.language.psi.PsiDocCommentBase;
import consulo.language.psi.PsiElement;

public interface DlangDocComment extends PsiDocCommentBase {

    PsiElement[] sections();
}

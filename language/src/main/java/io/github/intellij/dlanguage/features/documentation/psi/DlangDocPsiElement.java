package io.github.intellij.dlanguage.features.documentation.psi;

import consulo.language.psi.PsiElement;

public interface DlangDocPsiElement extends PsiElement {
    PsiElement[] getDescriptionElements();
}

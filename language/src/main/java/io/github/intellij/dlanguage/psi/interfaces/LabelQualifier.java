package io.github.intellij.dlanguage.psi.interfaces;

import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Represent elements that has qualifier to a label (goto, break or continue)
 */
public interface LabelQualifier extends PsiElement {
    @Nullable PsiElement getIdentifier();
}

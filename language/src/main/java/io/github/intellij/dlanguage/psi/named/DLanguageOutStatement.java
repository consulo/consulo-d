package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import org.jspecify.annotations.Nullable;

public interface DLanguageOutStatement extends DNamedElement, DTypedElement {

    @Nullable PsiElement getIdentifier();

    @Nullable DLanguageBlockStatement getBlockStatement();

    @Nullable PsiElement getKW_OUT();

    @Nullable PsiElement getOP_PAR_LEFT();

    @Nullable PsiElement getOP_PAR_RIGHT();
}

package io.github.intellij.dlanguage.psi;

import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;

public class DResolveResult implements ResolveResult {
    private final PsiElement myElement;

    public DResolveResult(PsiElement element) {
        myElement = element;
    }

    @Override
    public PsiElement getElement() {
        return myElement;
    }

    @Override
    public boolean isValidResult() {
        return true;
    }
}

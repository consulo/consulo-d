package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.stubs.DLanguageDeclaratorIdentifierStub;
import org.jspecify.annotations.Nullable;

public interface DLanguageDeclaratorIdentifier extends PsiElement, DNamedElement, DTypedElement,
    StubBasedPsiElement<DLanguageDeclaratorIdentifierStub> {

    @Nullable PsiElement getIdentifier();

    @Nullable PsiElement getOP_COMMA();
}


package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageInitializer;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.stubs.DLanguageAutoAssignmentStub;
import org.jspecify.annotations.Nullable;


public interface DLanguageAutoAssignment extends PsiElement, DNamedElement, DTypedElement,
    StubBasedPsiElement<DLanguageAutoAssignmentStub> {

    @Nullable PsiElement getIdentifier();

    @Nullable DLanguageTemplateParameters getTemplateParameters();

    @Nullable PsiElement getOP_EQ();

    @Nullable DLanguageInitializer getInitializer();
}

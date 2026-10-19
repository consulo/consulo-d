
package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.stubs.DLanguageForeachTypeStub;
import org.jspecify.annotations.Nullable;


public interface DLanguageForeachType extends PsiElement, DNamedElement, DTypedElement,
    StubBasedPsiElement<DLanguageForeachTypeStub> {

    @Nullable PsiElement getKW_REF();

    @Nullable PsiElement getKW_ALIAS();

    @Nullable PsiElement getKW_ENUM();

    @Nullable DLanguageBasicType getBasicType();

    @Nullable PsiElement getIdentifier();

    @Nullable PsiElement getOP_EQ();

    @Nullable Expression getExpression();
}

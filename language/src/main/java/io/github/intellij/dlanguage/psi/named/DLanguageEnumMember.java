
package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageEnumMemberAttribute;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.stubs.DLanguageEnumMemberStub;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageEnumMember extends PsiElement, DNamedElement, DTypedElement,
    StubBasedPsiElement<DLanguageEnumMemberStub> {

    List<DLanguageEnumMemberAttribute> getEnumMemberAttributes();

    @Nullable PsiElement getIdentifier();

    @Nullable PsiElement getOP_EQ();

    @Nullable DLanguageType getType();

    @Nullable Expression getExpression();
}

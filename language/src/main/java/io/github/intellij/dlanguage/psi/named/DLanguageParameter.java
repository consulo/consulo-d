
package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageAssignExpression;
import io.github.intellij.dlanguage.psi.DLanguageParameterAttribute;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DLanguageTypeSuffix;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.stubs.DLanguageParameterStub;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageParameter extends PsiElement, DNamedElement, DTypedElement,
    StubBasedPsiElement<DLanguageParameterStub> {

    List<DLanguageParameterAttribute> getParameterAttributes();

    @Nullable DLanguageType getType();

    @Nullable PsiElement getIdentifier();

    List<DLanguageTypeSuffix> getTypeSuffixs();

    @Nullable PsiElement getOP_TRIPLEDOT();

    @Nullable DLanguageAssignExpression getAssignExpression();

    @Nullable PsiElement getOP_EQ();
    @Nullable PsiElement getKW_IMMUTABLE();
    @Nullable PsiElement getKW_SHARED();
    @Nullable PsiElement getKW_FINAL();
    @Nullable PsiElement getKW_CONST();
    @Nullable PsiElement getKW_INOUT();
    @Nullable PsiElement getKW_IN();
    @Nullable PsiElement getKW_LAZY();
    @Nullable PsiElement getKW_OUT();
    @Nullable PsiElement getKW_REF();
    @Nullable PsiElement getKW_SCOPE();
    @Nullable PsiElement getKW_AUTO();
    @Nullable PsiElement getKW_RETURN();
}

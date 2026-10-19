
package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.stubs.DLanguageVersionSpecificationStub;
import org.jspecify.annotations.Nullable;


public interface DLanguageVersionSpecification extends PsiElement, DNamedElement, Declaration,
    StubBasedPsiElement<DLanguageVersionSpecificationStub> {

    @Nullable PsiElement getKW_VERSION();

    @Nullable PsiElement getOP_EQ();

    @Nullable PsiElement getIdentifier();

    @Nullable PsiElement getOP_PAR_RIGHT();

    @Nullable PsiElement getOP_PAR_LEFT();

    @Nullable PsiElement getINTEGER_LITERAL();

    @Nullable PsiElement getOP_SCOLON();

}

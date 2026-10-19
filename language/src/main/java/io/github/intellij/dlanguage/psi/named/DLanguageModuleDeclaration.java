package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageAtAttribute;
import io.github.intellij.dlanguage.psi.DLanguageDeprecated;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.stubs.DLanguageModuleDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageModuleDeclaration extends PsiElement, DNamedElement, Declaration,
    StubBasedPsiElement<DLanguageModuleDeclarationStub> {

    @Nullable List<DLanguageAtAttribute> getAtAttributes();

    @Nullable DLanguageDeprecated getDeprecated();

    @Nullable PsiElement getKW_MODULE();

    @Nullable DLanguageIdentifierChain getIdentifierChain();

    @Nullable PsiElement getOP_SCOLON();

}

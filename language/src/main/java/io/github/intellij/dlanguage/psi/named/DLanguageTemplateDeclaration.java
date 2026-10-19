package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageConstraint;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.HasMembers;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageTemplateDeclaration extends PsiElement, DNamedElement, Declaration, DTypedElement,
    StubBasedPsiElement<DLanguageTemplateDeclarationStub>, HasMembers<DLanguageTemplateDeclarationStub> {
    @Nullable PsiElement getKW_TEMPLATE();

    @Nullable PsiElement getIdentifier();

    @Nullable DLanguageTemplateParameters getTemplateParameters();

    @Nullable DLanguageConstraint getConstraint();

    @Nullable PsiElement getOP_BRACES_RIGHT();

    @Nullable PsiElement getOP_BRACES_LEFT();

    List<Declaration> getDeclarations();
}

package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageBaseClassList;
import io.github.intellij.dlanguage.psi.DLanguageConstraint;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.HasMembers;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.stubs.DLanguageInterfaceDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageInterfaceDeclaration extends PsiElement, DNamedElement, Declaration, UserDefinedType,
    StubBasedPsiElement<DLanguageInterfaceDeclarationStub>, HasMembers<DLanguageInterfaceDeclarationStub> {
    @Nullable PsiElement getKW_INTERFACE();

    @Nullable PsiElement getIdentifier();

    @Nullable DLanguageTemplateParameters getTemplateParameters();

    @Nullable PsiElement getOP_COLON();

    @Nullable DLanguageStructBody getStructBody();

    @Nullable DLanguageConstraint getConstraint();

    @Nullable DLanguageBaseClassList getBaseClassList();

    List<DLanguageInterfaceDeclaration> getInterfaces();
}

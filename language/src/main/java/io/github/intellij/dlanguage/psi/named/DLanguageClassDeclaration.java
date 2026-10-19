package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiQualifiedNamedElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageBaseClassList;
import io.github.intellij.dlanguage.psi.DLanguageConstraint;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.HasMembers;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.stubs.DLanguageClassDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageClassDeclaration extends PsiQualifiedNamedElement, DNamedElement, Declaration, UserDefinedType,
    StubBasedPsiElement<DLanguageClassDeclarationStub>, HasMembers<DLanguageClassDeclarationStub> {
    @Nullable PsiElement getKW_CLASS();

    @Nullable PsiElement getIdentifier();

    @Nullable DLanguageTemplateParameters getTemplateParameters();

    @Nullable PsiElement getOP_COLON();

    @Nullable DLanguageStructBody getStructBody();

    @Nullable DLanguageConstraint getConstraint();

    @Nullable DLanguageBaseClassList getBaseClassList();

    @Nullable DLanguageClassDeclaration getParentClass();

    List<DLanguageInterfaceDeclaration> getInterfaces();
}

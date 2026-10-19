
package io.github.intellij.dlanguage.psi.named;

import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.FunctionBody;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.stubs.DLanguageFunctionDeclarationStub;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;


public interface DLanguageFunctionDeclaration extends PsiElement, DNamedElement, Declaration,
    StubBasedPsiElement<DLanguageFunctionDeclarationStub> {

    @Nullable DLanguageBasicType getBasicType();

    List<DLanguageTypeSuffix> getTypeSuffixes();

    @Nullable PsiElement getIdentifier();

    @Nullable DLanguageTemplateParameters getTemplateParameters();

    @Nullable DLanguageParameters getParameters();

    @Nullable DLanguageConstraint getConstraint();

    @Nullable FunctionBody getFunctionBody();

    List<DLanguageMemberFunctionAttribute> getMemberFunctionAttributes();

    List<DLanguageStorageClass> getStorageClasses();

    default @Nullable DLanguageStorageClass getAutoElem() {
        for (DLanguageStorageClass storageClass : getStorageClasses()) {
            if(storageClass.getKW_AUTO() != null)
                return storageClass;
        }
        return null;
    }

    default boolean isAuto() {
        return getAutoElem() != null;

    }

    @Contract(pure = true)
    DType getReturnDType();
}

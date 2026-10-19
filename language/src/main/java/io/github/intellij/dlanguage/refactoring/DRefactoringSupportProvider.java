package io.github.intellij.dlanguage.refactoring;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.refactoring.RefactoringSupportProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;

/**
 * Created by francis on 4/18/2017.
 */
@ExtensionImpl
public class DRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isSafeDeleteAvailable(PsiElement element) {
        if (!(element instanceof DNamedElement)) {
            return false;
        }
        PsiReference reference = element.getReference();
        if (reference instanceof PsiPolyVariantReference polyVariantReference) {
            return polyVariantReference.multiResolve(false).length == 0;
        }
        return reference == null || reference.resolve() == null;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

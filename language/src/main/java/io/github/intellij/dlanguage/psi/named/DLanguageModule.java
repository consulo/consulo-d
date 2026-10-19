package io.github.intellij.dlanguage.psi.named;

import consulo.navigation.NavigationItem;
import consulo.language.psi.PsiCheckedRenameElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiQualifiedNamedElement;
import consulo.language.psi.scope.GlobalSearchScope;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import org.jspecify.annotations.Nullable;

/**
 * Represents a Dlang package.
 */
public interface DLanguageModule extends DNamedElement, PsiCheckedRenameElement, NavigationItem,
    PsiQualifiedNamedElement {

    DLanguageModule[] EMPTY_ARRAY = new DLanguageModule[0];

    @Override
    String getQualifiedName();

    /**
     * Returns the parent of the package.
     *
     * @return the parent package, or null for no package.
     */
    @Nullable DLanguagePackage getParentPackage();

    /**
     * Returns the file that represents the module.
     */
    PsiFile getFile(GlobalSearchScope searchScope);

    @Override
    String getName();
}

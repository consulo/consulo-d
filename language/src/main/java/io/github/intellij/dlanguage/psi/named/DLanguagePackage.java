package io.github.intellij.dlanguage.psi.named;

import consulo.navigation.NavigationItem;
import consulo.language.psi.PsiCheckedRenameElement;
import consulo.language.psi.PsiDirectoryContainer;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiQualifiedNamedElement;
import consulo.language.psi.scope.GlobalSearchScope;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import org.jspecify.annotations.Nullable;

/**
 * Represents a Dlang package.
 */
public interface DLanguagePackage extends DNamedElement, PsiCheckedRenameElement, NavigationItem,
    PsiDirectoryContainer, PsiQualifiedNamedElement {

    DLanguagePackage[] EMPTY_ARRAY = new DLanguagePackage[0];

    @Override
    String getQualifiedName();

    /**
     * Returns the parent of the package.
     *
     * @return the parent package, or null for no package.
     */
    @Nullable DLanguagePackage getParentPackage();

    /**
     * Return the list of subpackages of this package under all source roots of the project.
     *
     * @return the array of subpackages.
     */
    DLanguagePackage[] getSubPackages();

    /**
     * Return the list of subpackages of this package in the specified search scope.
     *
     * @param scope the scope in which packages are searches.
     * @return the array of subpackages.
     */
    DLanguagePackage[] getSubPackages(GlobalSearchScope scope);

    /**
     *
     */
    PsiFile[] getFiles(GlobalSearchScope searchScope);

    /**
     * This method must be invoked on the package after all directories corresponding
     * to it have been renamed/moved accordingly to the qualified name change.
     *
     * @param newQualifiedName the new qualified name of the package.
     */
    void handleQualifiedNameChange(String newQualifiedName);

    @Override
    String getName();
}

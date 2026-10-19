package io.github.intellij.dlanguage.psi;

import consulo.language.psi.PsiFile;
import org.jspecify.annotations.Nullable;

/**
 * @author Samael Bate (singingbush)
 * created on 15/02/2022
 */
public interface DlangPsiFile extends PsiFile {

    /**
     * D module names are, by default, the file name with the path and extension stripped off.
     * They can be set explicitly with the module declaration.
     * This method attempts to find an explicitly defined module declaration first and then return the filename (without extension)
     * if no module declaration is found.
     */
    String getModuleName();

    /**
     * A fully qualified module name is the package (if defined) followed by the module name.
     * Both should be lowercase ASCII characters (letters, digits, underscore). Packages correspond
     * to directory names in the source file path. Package and module names cannot be Keywords.
     * <p>
     * An example of such would be "c.stdio", where the stdio module is in the c package.
     * <p>
     * If the module name is not defined within the file then the filename (without extension) is used as the module name.
     */
    String getFullyQualifiedModuleName();

    /**
     * Packages correspond to directory names in the source file path. As a D module doesn't need to exist
     * within a package this method can return null. As with module names, package names cannot be keywords.
     */
    @Nullable String getPackageName();
}

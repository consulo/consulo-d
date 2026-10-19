package io.github.intellij.dlanguage.unittest;

import consulo.fileTemplate.FileTemplateDescriptor;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

/**
 * A base class to be used by any future D test frameworks.
 *
 * d-unit support provided by DUnitTestFramework.class
 *
 * In the future I'd like to support other options such as:
 *      Unit Threaded (https://code.dlang.org/packages/unit-threaded)
 *      Silly (https://code.dlang.org/packages/silly)
 *      DUnit (https://code.dlang.org/packages/)
 *      trial (https://code.dlang.org/packages/trial)
 *
 * see https://github.com/intellij-dlanguage/intellij-dlanguage/issues/431
 * <p>
 * Port of upstream {@code DlangTestFramework.kt}. Consulo's platform has no {@code TestFramework} extension point
 * (consulo-java's is Java only, and upstream never registered this one), so the methods of IntelliJ's interface that
 * the subclasses implement are declared here.
 *
 * @author Samael Bate (singingbush)
 * created on 06/11/2019
 */
public abstract class DlangTestFramework {

    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    public LocalizeValue getName() {
        return DLanguage.INSTANCE.getDisplayName();
    }

    public Image getIcon() {
        return PlatformIconGroup.runconfigurationsJunit();
    }

    public @Nullable String getLibraryPath() {
        return null; // todo: Should be able to get this
    }

    public boolean isLibraryAttached(Module module) {
        return false;
    }

    public @Nullable String getDefaultSuperClass() {
        return null;
    }

    public boolean isPotentialTestClass(PsiElement clazz) {
        return isTestClass(clazz);
    }

    public boolean isTestClass(PsiElement clazz) {
        return true;
    }

    public abstract @Nullable PsiElement findSetUpMethod(PsiElement clazz);

    public abstract @Nullable PsiElement findTearDownMethod(PsiElement clazz);

    public abstract @Nullable PsiElement findOrCreateSetUpMethod(PsiElement clazz) throws IncorrectOperationException;

    public abstract @Nullable FileTemplateDescriptor getSetUpMethodFileTemplateDescriptor();

    public abstract @Nullable FileTemplateDescriptor getTearDownMethodFileTemplateDescriptor();

    public abstract @Nullable FileTemplateDescriptor getTestMethodFileTemplateDescriptor();

    public abstract boolean isIgnoredMethod(PsiElement element);

    public abstract boolean isTestMethod(PsiElement element);
}

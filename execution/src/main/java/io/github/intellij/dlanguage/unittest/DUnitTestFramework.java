package io.github.intellij.dlanguage.unittest;

import consulo.fileTemplate.FileTemplateDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageAtAttribute;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Provides the support for d-unit
 */
public class DUnitTestFramework extends DlangTestFramework {

    @Override
    public @Nullable PsiElement findSetUpMethod(final PsiElement clazz) {
        // TODO: However you find a setup method, if applicable. For example, in mine they’re annotated with @TestSetup.
        return null;
    }

    @Override
    public @Nullable PsiElement findTearDownMethod(final PsiElement clazz) {
        // TODO: However you find a teardown method, if applicable.  Apex doesn’t have that notion.
        return null;
    }

    @Override
    public @Nullable PsiElement findOrCreateSetUpMethod(final PsiElement clazz) throws IncorrectOperationException {
        final PsiElement setUpMethod = findSetUpMethod(clazz);
        if (setUpMethod != null) {
            return setUpMethod;
        } else {
            // TODO: Add support for this
            throw new IncorrectOperationException("No @TestSetup method and creation is not yet supported.");
        }
    }

    @Override
    public @Nullable FileTemplateDescriptor getSetUpMethodFileTemplateDescriptor() {
        // TODO: Add support for this
        return null;
    }

    @Override
    public @Nullable FileTemplateDescriptor getTearDownMethodFileTemplateDescriptor() {
        return null;
    }

    @Override
    public @Nullable FileTemplateDescriptor getTestMethodFileTemplateDescriptor() {
        // TODO: Add support for this
        return null;
    }

    @Override
    public boolean isIgnoredMethod(final PsiElement element) {
        return false;
    }

    @Override
    public boolean isTestMethod(final PsiElement element) {
        // TODO: However you determine whether a method is a test method.  For example, in mine they’re annotated with @Test or the testMethod modifier

        if (element.getLanguage() == DLanguage.INSTANCE) {
            final Collection<DLanguageAtAttribute> udas = PsiTreeUtil.findChildrenOfType(element, DLanguageAtAttribute.class);
            return !udas.isEmpty();
        } else {
            return false;
        }
    }

}

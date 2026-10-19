package io.github.intellij.dlanguage.psi.impl;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedVariableDeclaration;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import org.jspecify.annotations.Nullable;

import static consulo.language.psi.util.PsiTreeUtil.findChildOfType;

/**
 * Performs creation of element types.
 */
public class DElementFactory {

    /**
     * Takes a name and returns a Psi node of that name, or null.
     */
    public static @Nullable PsiElement createDLanguageIdentifierFromText(final Project project, final String name) {
        if (name.isBlank()) return null;
        @Nullable PsiElement element = createExpressionFromText(project, "int " + name + ";");
        assert element != null;
        final PsiElement e = ((DLanguageSpecifiedVariableDeclaration) element).getIdentifierInitializers().get(0).getIdentifier();

        return e != null && e.getText().equals(name) ? e : null;
    }

    /**
     * Takes an expression in text and returns a Psi tree of that program.
     */
    private static @Nullable PsiElement createExpressionFromText(final Project project, final String name) {
        final DlangPsiFile fileFromText = createFileFromText(project, name);

        final @Nullable PsiElement firstChild = fileFromText.getFirstChild();

        return firstChild;
    }

    /**
     * Create a file containing text.
     */
    private static DlangPsiFile createFileFromText(final Project project,
                                                final String text) {
        return (DlangPsiFile) PsiFileFactory.getInstance(project)
            .createFileFromText("A.d", DLanguage.INSTANCE, text);
    }

    public static @Nullable DLanguageModuleDeclaration createDLanguageModuleFromText(final Project project,
                                                                           final String name) {
        if(StringUtil.isEmptyOrSpaces(name)) {
            return null; // perhaps should throw exception
        }
        return findChildOfType(
            createFileFromText(project, "module " + name + ";"),
            DLanguageModuleDeclaration.class
        );
    }

    public static @Nullable DLanguageAliasDeclaration createAliasDeclarationFromText(final Project project,
                                                                           final String text) {
        final PsiFile fileFromText = PsiFileFactory.getInstance(project)
                                                   .createFileFromText("A.d", DLanguage.INSTANCE, text);
        return findChildOfType(fileFromText, DLanguageAliasDeclaration.class);
    }
}

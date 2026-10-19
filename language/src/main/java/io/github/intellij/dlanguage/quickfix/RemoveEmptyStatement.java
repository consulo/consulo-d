package io.github.intellij.dlanguage.quickfix;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.DLanguageEmptyStatement;

/**
 * Created by francis on 11/25/2017.
 */
public class RemoveEmptyStatement extends LocalQuickFixOnPsiElement {
    public RemoveEmptyStatement(DLanguageEmptyStatement elem) {
        super(elem);
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixRemoveemptystatementText();
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        getStartElement().delete();
    }

}

package io.github.intellij.dlanguage.quickfix;

import consulo.d.localize.DLocalize;
import consulo.document.Document;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.Objects;

/**
 * Created by francis on 11/26/2017.
 */
public class MakeFunctionConst extends LocalQuickFixOnPsiElement {
    public MakeFunctionConst(DLanguageFunctionDeclaration elem) {
        super(elem);
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixMakefunctionconstText();
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        DLanguageFunctionDeclaration funcDecl = (DLanguageFunctionDeclaration) startElement;
        int endOffset = Objects.requireNonNull(funcDecl.getParameters()).getTextRange().getEndOffset();
        Document document = Objects.requireNonNull(file.getViewProvider().getDocument());
        document.insertString(endOffset, " const");
    }

}

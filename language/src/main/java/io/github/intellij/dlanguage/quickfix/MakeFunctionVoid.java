package io.github.intellij.dlanguage.quickfix;

import consulo.d.localize.DLocalize;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.Objects;

public class MakeFunctionVoid extends LocalQuickFixOnPsiElement {
    public MakeFunctionVoid(DLanguageFunctionDeclaration elem) {
        super(elem);
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixMakefunctionvoidText();
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        DLanguageFunctionDeclaration funcDecl = (DLanguageFunctionDeclaration) startElement;
        DLanguageStorageClass autoElem = Objects.requireNonNull(funcDecl.getAutoElem());
        TextRange range = autoElem.getTextRange();
        Document document = Objects.requireNonNull(file.getViewProvider().getDocument());
        document.replaceString(range.getStartOffset(), range.getEndOffset(), "void");
    }

}

package io.github.intellij.dlanguage.quickfix;

import consulo.d.localize.DLocalize;
import consulo.document.Document;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.DLanguageAutoDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;

import java.util.Objects;

/**
 * Created by francis on 1/5/2018.
 */
public class MakeStaticImmutable extends LocalQuickFixOnPsiElement {
    public MakeStaticImmutable(PsiElement elem) {
        super(elem);
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixMakestaticimmutableText();
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        DLanguageAutoDeclaration autoDeclarationOverall = (DLanguageAutoDeclaration) Objects.requireNonNull(startElement.getParent());
        for (DLanguageStorageClass storageClasss : autoDeclarationOverall.getStorageClasss()) {
            if (storageClasss.getKW_ENUM() != null) {
                storageClasss.delete();
            }
        }
        int startOffset = autoDeclarationOverall.getTextRange().getStartOffset();
        Document document = Objects.requireNonNull(file.getViewProvider().getDocument());
        document.insertString(startOffset, " static immutable ");
    }

}

package io.github.intellij.dlanguage.quickfix;

import consulo.application.Application;
import consulo.d.localize.DLocalize;
import consulo.document.Document;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.DLanguageLiteralExpression;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Created by francis on 11/23/2017.
 */
public class ExplicitlyConcatenateStrings extends LocalQuickFixOnPsiElement {

    public ExplicitlyConcatenateStrings(DLanguageLiteralExpression elem) {
        super(elem, elem);
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixExplicitlyconcatenatestringsText();
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        Application.get().runWriteAction(() -> {

            DLanguageLiteralExpression literalExpression = (DLanguageLiteralExpression) getStartElement();
            Document doc = Objects.requireNonNull(file.getViewProvider().getDocument());
            @Nullable PsiElement last = null;
            int i = 0;
            for (PsiElement string : literalExpression.getChildren()) {
                if (last != null) {
                    int insertLocation = last.getTextOffset() + last.getTextLength() + i;
                    doc.insertString(insertLocation, " ~");
                    i += 2; //this i is used to handle the offset becoming out of sync as we write to the file
                }
                last = string;
            }
        });
    }

}

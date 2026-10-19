package io.github.intellij.dlanguage.quickfix;

import consulo.application.Application;
import consulo.d.localize.DLocalize;
import consulo.document.Document;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;

import java.util.Objects;

public class FormatNumber extends LocalQuickFixOnPsiElement {
    public FormatNumber(PsiElement elem) {
        super(elem);
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        String text = startElement.getText().replace("_", "");

        String withUnderScores = getWithUnderScores(text);
        Application.get().runWriteAction(() -> {
            Document doc = Objects.requireNonNull(file.getViewProvider().getDocument());
            doc.replaceString(startElement.getTextRange().getStartOffset(), startElement.getTextRange().getEndOffset(), withUnderScores);
        });

    }

    private String getWithUnderScores(String text) {
        int len = text.length();
        if (len < 4) {
            return text;
        }
        return getWithUnderScores(text.substring(0, len - 3)) + "_" + text.subSequence(len - 3, text.length());
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixFormatnumberText();
    }
}

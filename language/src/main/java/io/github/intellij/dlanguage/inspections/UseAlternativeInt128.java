package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DlangPsiFile;

import java.util.Objects;

public class UseAlternativeInt128 extends LocalQuickFixOnPsiElement {
    private final String myReplacement;

    public UseAlternativeInt128(DLanguageBasicType type, String replacement) {
        super(type);
        myReplacement = replacement;
    }

    public String getReplacement() {
        return myReplacement;
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixUsealternativeint128Text(myReplacement);
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        DlangPsiFile dlangFile = (DlangPsiFile) PsiFileFactory.getInstance(project)
            .createFileFromText("tmp", DlangFileType.INSTANCE, myReplacement);
        getStartElement().replace(Objects.requireNonNull(dlangFile.getFirstChild()));
    }
}

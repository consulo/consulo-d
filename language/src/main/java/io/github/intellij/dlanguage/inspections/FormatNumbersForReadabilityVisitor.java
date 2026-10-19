package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.quickfix.FormatNumber;

public class FormatNumbersForReadabilityVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public FormatNumbersForReadabilityVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitElement(PsiElement element) {
        String text = element.getText();
        int length = text.length();
        if (isNumber(element) && length > 3) {
            if (!isCorrectlyFormatted(text)) {
                myHolder.newProblem(DLocalize.dInspectionsSymbolFormatnumbersforreadabilityMessage())
                    .range(element)
                    .withFixes(new FormatNumber(element))
                    .create();
            }
        }
    }

    public boolean isCorrectlyFormatted(String text) {
        int length = text.length();
        for (int i = length - 4; i >= 0; i -= 4) {
            if (text.charAt(i) != '_') {
                return false;
            }
        }
        return true;

    }

    public boolean isNumber(PsiElement element) {
        if (!(element instanceof LeafPsiElement leafPsiElement)) {
            return false;
        }
        return leafPsiElement.getElementType() == DlangTypes.INTEGER_LITERAL;
    }
}

package io.github.intellij.dlanguage.template;

import consulo.language.Language;
import consulo.language.editor.surroundWith.SurroundDescriptor;
import consulo.language.editor.surroundWith.Surrounder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.DLanguage;

// A placeholder for later to enable language specific template expansion.
// todo: revisit this code and do something useful with it
public class DlangExpressionSurroundDescriptor implements SurroundDescriptor {

    private static final Surrounder[] SURROUNDERS = {
        new ExampleSurrounder()
        /*DlangWithParenthesesSurrounder(),
        DlangWithNotSurrounder(),
        DlangWithIfExpSurrounder(),
        DlangWithWhileExpSurrounder()*/
    };

    @Override
    public PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
        return PsiElement.EMPTY_ARRAY;

        // because com.intellij.codeInsight.CodeInsightUtil.findExpressionInRange isn't available in CLion and this class
        // is currently dead code anyway, I've commented it out for now

//        val expr = findExpressionInRange(file, startOffset, endOffset) ?: return emptyArray()
//        FeatureUsageTracker.getInstance().triggerFeatureUsed("codeassists.surroundwith.expression")
//        return arrayOf(expr)
    }

    @Override
    public Surrounder[] getSurrounders() {
        return SURROUNDERS;
    }

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

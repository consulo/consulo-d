package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageLiteralExpression;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.quickfix.ExplicitlyConcatenateStrings;

public class ImplicitlyConcatenatedStringsVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public ImplicitlyConcatenatedStringsVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitLiteralExpression(DLanguageLiteralExpression o) {
        if (o.getDOUBLE_QUOTED_STRINGs().size() + o.getDELIMITED_STRINGs().size() + o.getALTERNATE_WYSIWYG_STRINGs().size()
            + o.getWYSIWYG_STRINGs().size() + o.getHEX_STRINGs().size() > 1) {
            myHolder.newProblem(DLocalize.dInspectionsSymbolImplicitlyconcatenatedstringsMessage())
                .range(o)
                .withFixes(new ExplicitlyConcatenateStrings(o))
                .create();
        }
    }
}

package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageRelExpression;
import io.github.intellij.dlanguage.psi.DlangVisitor;

public class FishOperatorsVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public FishOperatorsVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitRelExpression(DLanguageRelExpression o) {
        if (o.getOP_GT() != null || o.getOP_GT_EQ() != null || o.getOP_LESS() != null || o.getOP_LESS_EQ() != null || o.getOP_NOT_GR() != null || o.getOP_NOT_LESS() != null || o.getOP_NOT_GR_EQ() != null) {
            myHolder.newProblem(DLocalize.dInspectionsSymbolFishoperatorsMessage())
                .range(o)
                .create();
        }
    }
}

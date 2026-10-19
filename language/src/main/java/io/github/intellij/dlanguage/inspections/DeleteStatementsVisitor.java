package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageDeleteExpression;
import io.github.intellij.dlanguage.psi.DLanguageDeleteStatement;
import io.github.intellij.dlanguage.psi.DlangVisitor;

public class DeleteStatementsVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public DeleteStatementsVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitDeleteExpression(DLanguageDeleteExpression o) {
        register(o);
    }

    @Override
    public void visitDeleteStatement(DLanguageDeleteStatement o) {
        register(o);
    }

    public void register(PsiElement o) {
        myHolder.newProblem(DLocalize.dInspectionsSymbolDeletestatementsaredeprecatedMessage())
            .range(o)
            .create();
    }
}

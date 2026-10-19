package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageEmptyStatement;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.quickfix.RemoveEmptyStatement;

public class EmptyStatementVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public EmptyStatementVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitEmptyStatement(DLanguageEmptyStatement o) {
        myHolder.newProblem(DLocalize.dInspectionsSymbolEmptystatementMessage())
            .range(o)
            .withFixes(new RemoveEmptyStatement(o))
            .create();
    }
}

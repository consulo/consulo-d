package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageDeleteExpression;
import io.github.intellij.dlanguage.psi.DlangVisitor;

public class DeleteIsDeprecatedVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public DeleteIsDeprecatedVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitDeleteExpression(DLanguageDeleteExpression o) {
        myHolder.newProblem(DLocalize.dInspectionsSymbolDeleteisdeprecatedMessage())
            .range(o)
            .create();
    }
}

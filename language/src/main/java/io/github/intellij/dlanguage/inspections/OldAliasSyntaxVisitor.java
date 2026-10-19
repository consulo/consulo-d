package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.quickfix.SwitchToNewAliasSyntax;

public class OldAliasSyntaxVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public OldAliasSyntaxVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitAliasDeclaration(DLanguageAliasDeclaration o) {
        if (o.getType() == null) {
            return;
        }
        myHolder.newProblem(DLocalize.dInspectionsSymbolOldaliassyntaxMessage())
            .range(o)
            .withFixes(new SwitchToNewAliasSyntax(o))
            .create();
    }
}

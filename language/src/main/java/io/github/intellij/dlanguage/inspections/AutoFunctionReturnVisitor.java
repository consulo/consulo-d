package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageReturnStatement;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.quickfix.MakeFunctionVoid;

import java.util.Collection;

public class AutoFunctionReturnVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public AutoFunctionReturnVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitFunctionDeclaration(DLanguageFunctionDeclaration o) {
        if (shouldWarn(o)) {
            myHolder.newProblem(DLocalize.dInspectionsSymbolAutofunctionwithoutreturnMessage())
                .range(o)
                .withFixes(new MakeFunctionVoid(o))
                .create();
        }
    }

    private boolean shouldWarn(DLanguageFunctionDeclaration o) {
        if (!o.isAuto()) {
            return false;
        }
        Collection<DLanguageReturnStatement> returnStatements = PsiTreeUtil.findChildrenOfType(o, DLanguageReturnStatement.class);
        for (DLanguageReturnStatement returnStatement : returnStatements) {
            if (!returnStatement.getExpressions().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

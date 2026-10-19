package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageAssignExpression;
import io.github.intellij.dlanguage.psi.DLanguageIfCondition;
import io.github.intellij.dlanguage.psi.DLanguageIfStatement;
import io.github.intellij.dlanguage.psi.DlangVisitor;

import java.util.List;
import java.util.Objects;

/**
 * todo this is not done.
 */
public class RedundantParenthesesVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public RedundantParenthesesVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitIfStatement(DLanguageIfStatement o) {
        DLanguageIfCondition ifCondition = o.getIfCondition();
        List<DLanguageAssignExpression> expressions = ifCondition != null ? ifCondition.getAssignExpressions() : null;
        if (expressions == null || expressions.size() != 1) {
            return;
        }
        if (expressions.get(0) == null) {
            return;
        }
        myHolder.newProblem(DLocalize.dInspectionsSymbolRedundantparenthesesMessage())
            .range(Objects.requireNonNull(o.getIfCondition()))
            .create();
    }

    //override fun visitPrimaryExpression(o: DLanguagePrimaryExpression) {
    //    /*if (o.assignExpressions.isEmpty() || o.assignExpressions[0] == null)
    //        return
    //    val unary = o.assignExpressions[0]
    //    if (unary.assignExpression == null || unary.assignExpression!!.assignExpression == null)
    //        return
    //    holder.registerProblem(o, "Redundant parantheses")*/
    //}
}

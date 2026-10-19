package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageParenthesisedExpression;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageParenthesisedExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageParenthesisedExpression {
    public DLanguageParenthesisedExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        Expression expression = getExpression();
        return expression != null ? expression.getDType() : null;
    }
}

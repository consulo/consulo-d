package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageFunctionCallExpression;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageFunctionCallExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageFunctionCallExpression {
    public DLanguageFunctionCallExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        Expression expression = getExpression();
        return expression != null ? expression.getDType() : null;
    }
}

package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageMulExpression;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageMulExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageMulExpression {
    public DLanguageMulExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        // TODO (arithmetic conversion)
        return getExpressions().getFirst().getDType();
    }
}

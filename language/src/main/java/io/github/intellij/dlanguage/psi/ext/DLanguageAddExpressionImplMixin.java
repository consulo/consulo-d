package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageAddExpression;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageAddExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageAddExpression {
    public DLanguageAddExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        // TODO (arithmetic conversion)
        return getExpressions().getFirst().getDType();
    }
}

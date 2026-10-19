package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageCastExpression;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageCastExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageCastExpression {
    public DLanguageCastExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        DLanguageType type = getType();
        return type != null ? type.getDType() : null;
    }
}

package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageDollarExpression;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageDollarExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageDollarExpression {
    public DLanguageDollarExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        return DPrimitiveType.fromText("int"); // TODO size_t
    }
}

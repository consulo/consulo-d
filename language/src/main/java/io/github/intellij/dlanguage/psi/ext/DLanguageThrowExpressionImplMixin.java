package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageThrowExpression;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageThrowExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageThrowExpression {
    public DLanguageThrowExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        return DPrimitiveType.fromText("void"); // Note: actually noreturn but void is fine for now
    }
}

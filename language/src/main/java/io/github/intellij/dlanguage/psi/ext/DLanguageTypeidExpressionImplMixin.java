package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageTypeidExpression;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageTypeidExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageTypeidExpression {
    public DLanguageTypeidExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        return new DUnknownType(); // TODO Typeinfo instance (typeinfo class being defined in object)
    }
}

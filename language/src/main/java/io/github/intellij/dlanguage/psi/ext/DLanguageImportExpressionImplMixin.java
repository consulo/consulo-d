package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageImportExpression;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageImportExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageImportExpression {
    public DLanguageImportExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        return new DArrayType(DPrimitiveType.fromText("char"), null); // TODO hex string
    }
}

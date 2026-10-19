package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageArrayAccessExpression;
import io.github.intellij.dlanguage.psi.DLanguageIndexExpression;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DAssociativeArrayType;
import io.github.intellij.dlanguage.psi.types.DPointerType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageArrayAccessExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageArrayAccessExpression {
    public DLanguageArrayAccessExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        Expression expression = getExpression();
        if (expression == null) {
            return null;
        }
        DType type = expression.getDType();
        DLanguageIndexExpression indexExpression = getIndexExpression();
        // simple array access
        if (indexExpression == null || indexExpression.getExpressions().size() == 1) {
            if (type instanceof DArrayType arrayType) {
                return arrayType.getBase();
            }
            if (type instanceof DAssociativeArrayType associativeArrayType) {
                return associativeArrayType.getValueType();
            }
            return new DUnknownType();
        }
        // slice
        if (type instanceof DPointerType pointerType) {
            type = new DArrayType(pointerType.getBase(), null);
        }
        if (!(type instanceof DArrayType)) {
            type = new DArrayType(new DUnknownType(), null);
        }
        return type;
    }
}

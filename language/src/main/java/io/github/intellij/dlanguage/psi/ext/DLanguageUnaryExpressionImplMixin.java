package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageUnaryExpression;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.types.DPointerType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageUnaryExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageUnaryExpression {
    public DLanguageUnaryExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        // TODO handle operator overloading
        if (getOP_ASTERISK() != null) {
            DType type = getExpressionDType();
            if (type == null) {
                return null;
            }
            if (type instanceof DPointerType pointerType) {
                return pointerType.getBase();
            }
            // to dereference a pointer, it must be a pointer
            return new DUnknownType();
        }
        if (getOP_AND() != null) {
            DType type = getExpressionDType();
            return new DPointerType(type != null ? type : new DUnknownType());
        }
        return getExpressionDType();
    }

    private @Nullable DType getExpressionDType() {
        Expression expression = getExpression();
        return expression != null ? expression.getDType() : null;
    }
}

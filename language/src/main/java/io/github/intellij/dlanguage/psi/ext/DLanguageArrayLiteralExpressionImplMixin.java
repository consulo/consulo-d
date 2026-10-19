package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageArrayLiteral;
import io.github.intellij.dlanguage.psi.DLanguageArrayLiteralExpression;
import io.github.intellij.dlanguage.psi.DLanguageAssocArrayLiteral;
import io.github.intellij.dlanguage.psi.DLanguageKeyValuePair;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DAssociativeArrayType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static io.github.intellij.dlanguage.utils.DTypeConversionUtilKt.getCommonType;

public abstract class DLanguageArrayLiteralExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageArrayLiteralExpression {
    public DLanguageArrayLiteralExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        DLanguageAssocArrayLiteral assocArrayLiteral = getAssocArrayLiteral();
        if (assocArrayLiteral != null) {
            DType keyType = null;
            DType valueType = null;
            for (DLanguageKeyValuePair keyValue : Objects.requireNonNull(assocArrayLiteral.getKeyValuePairs()).getKeyValuePairs()) {
                if (keyValue.getExpressions().isEmpty()) {
                    continue;
                }
                keyType = getCommonType(keyType, keyValue.getExpressions().getFirst().getDType());
                if (keyValue.getExpressions().size() > 1) {
                    valueType = getCommonType(valueType, keyValue.getExpressions().get(1).getDType());
                }
            }
            return new DAssociativeArrayType(
                valueType != null ? valueType : new DUnknownType(),
                keyType != null ? keyType : new DUnknownType()
            );
        }
        DLanguageArrayLiteral arrayLiteral = getArrayLiteral();
        assert arrayLiteral != null;
        DType commonType = null;
        for (Expression expr : arrayLiteral.getExpressions()) {
            commonType = getCommonType(commonType, expr.getDType());
        }
        return new DArrayType(commonType != null ? commonType : new DUnknownType(), (long) arrayLiteral.getExpressions().size());
    }
}

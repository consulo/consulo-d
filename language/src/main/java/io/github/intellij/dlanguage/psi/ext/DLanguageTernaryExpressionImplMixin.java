package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageTernaryExpression;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract class DLanguageTernaryExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageTernaryExpression {
    public DLanguageTernaryExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        if (getExpressions().size() != 3) {
            return null;
        }
        // TODO handle aliased type
        if (Objects.equals(getExpressions().get(1).getDType(), DPrimitiveType.fromText("void"))
            || Objects.equals(getExpressions().get(1).getDType(), DPrimitiveType.fromText("void"))) {
            return DPrimitiveType.fromText("void");
        }
        // TODO actually it is the common type of both
        return getExpressions().get(1).getDType();
    }
}

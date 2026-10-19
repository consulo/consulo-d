package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageAndAndExpression;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract class DLanguageAndAndExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageAndAndExpression {
    public DLanguageAndAndExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        if (getExpressions().size() != 2) {
            return DPrimitiveType.fromText("bool");
        }
        if (Objects.equals(getExpressions().get(1).getDType(), DPrimitiveType.fromText("void"))) {
            return DPrimitiveType.fromText("void");
        }
        return DPrimitiveType.fromText("bool");
    }
}

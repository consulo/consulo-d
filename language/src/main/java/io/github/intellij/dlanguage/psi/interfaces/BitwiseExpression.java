package io.github.intellij.dlanguage.psi.interfaces;

import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface BitwiseExpression extends Expression {

    List<Expression> getExpressions();

    @Override
    default @Nullable DType getDType() {
        if (getExpressions().size() != 2)
            return null;
        if (!DPrimitiveType.fromText("int").equals(getExpressions().getFirst().getDType()) ||
            !DPrimitiveType.fromText("int").equals(getExpressions().get(1).getDType()))
            return null;
        return DPrimitiveType.fromText("int");
    }
}

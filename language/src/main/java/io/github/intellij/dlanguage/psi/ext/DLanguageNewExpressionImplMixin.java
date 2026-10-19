package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageNewExpression;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.types.DPointerType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.UserDefinedDType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageNewExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageNewExpression {
    public DLanguageNewExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        DLanguageType type = getType();
        if (type == null) {
            return null;
        }
        DType dtype = type.getDType();
        // TODO handle aliased type
        UserDefinedType resolved = dtype instanceof UserDefinedDType userDefinedDType ? userDefinedDType.resolve() : null;
        if (resolved instanceof DLanguageClassDeclaration) {
            // it is a reference type
            return dtype;
        }
        return new DPointerType(dtype);
    }
}

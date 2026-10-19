package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageFundamentalTypeConstructExpression;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageFundamentalTypeConstructExpressionImplMixin extends ASTWrapperPsiElement
    implements DLanguageFundamentalTypeConstructExpression {

    public DLanguageFundamentalTypeConstructExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        DLanguageBasicType basicType = getBasicType();
        return basicType != null ? basicType.getDType() : null;
    }
}

package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageAnonymousEnumDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;

public abstract class DLanguageAnonymousEnumDeclarationImplMixin extends ASTWrapperPsiElement implements DLanguageAnonymousEnumDeclaration {
    public DLanguageAnonymousEnumDeclarationImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public DType getDType() {
        DLanguageType type = getType();
        if (type != null) {
            return type.getDType();
        }
        return DPrimitiveType.INT;
    }
}

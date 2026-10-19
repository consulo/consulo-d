package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.psi.DLanguageTemplateThisParameter;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;

public abstract class DLanguageTemplateThisParameterImplMixin extends ASTWrapperPsiElement implements DLanguageTemplateThisParameter {
    public DLanguageTemplateThisParameterImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public DType getDType() {
        return new DUnknownType(); // TODO
    }
}

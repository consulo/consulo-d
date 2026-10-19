package io.github.intellij.dlanguage.features.documentation.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.List;

public class DDocParamsSectionImpl extends DDocNamedSectionImpl {
    public DDocParamsSectionImpl(ASTNode node) {
        super(node);
    }

    public List<DDocKeyValueImpl> getParameters() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DDocKeyValueImpl.class);
    }
}

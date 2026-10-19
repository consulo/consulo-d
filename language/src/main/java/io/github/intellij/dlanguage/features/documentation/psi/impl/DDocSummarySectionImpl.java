package io.github.intellij.dlanguage.features.documentation.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.features.documentation.DDocElementTypes;

import java.util.Objects;

public class DDocSummarySectionImpl extends DlangDocPsiElementImpl {
    public DDocSummarySectionImpl(ASTNode node) {
        super(node);
    }

    public PsiElement getSection() {
        return Objects.requireNonNull(findChildByType(DDocElementTypes.DDOC_SECTION_PARAGRAPH));
    }
}

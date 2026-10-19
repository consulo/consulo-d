package io.github.intellij.dlanguage.features.documentation.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.features.documentation.DDocElementTypes;

import java.util.List;

public class DDocDescriptionSectionImpl extends DlangDocPsiElementImpl {
    public DDocDescriptionSectionImpl(ASTNode node) {
        super(node);
    }

    public List<PsiElement> getSections() {
        return findChildrenByType(DDocElementTypes.DDOC_SECTION_PARAGRAPH);
    }
}

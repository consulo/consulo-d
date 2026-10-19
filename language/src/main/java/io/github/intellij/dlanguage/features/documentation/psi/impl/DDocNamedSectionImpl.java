package io.github.intellij.dlanguage.features.documentation.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;

import java.util.List;
import java.util.Objects;

import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_SECTION_PARAGRAPH;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_SECTION_TITLE;

public class DDocNamedSectionImpl extends DlangDocPsiElementImpl {
    public DDocNamedSectionImpl(ASTNode node) {
        super(node);
    }

    public PsiElement getTitle() {
        return Objects.requireNonNull(findChildByType(DDOC_SECTION_TITLE));
    }

    public List<PsiElement> getContents() {
        return findChildrenByType(DDOC_SECTION_PARAGRAPH);
    }

}

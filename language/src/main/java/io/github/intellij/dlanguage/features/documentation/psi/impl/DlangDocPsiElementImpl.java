package io.github.intellij.dlanguage.features.documentation.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes;
import io.github.intellij.dlanguage.features.documentation.psi.DlangDocPsiElement;

import java.util.ArrayList;
import java.util.List;

public class DlangDocPsiElementImpl extends ASTWrapperPsiElement implements DlangDocPsiElement {
    public DlangDocPsiElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    @RequiredReadAction
    public PsiElement[] getDescriptionElements() {
        List<PsiElement> array = new ArrayList<>();
        PsiElement child = getFirstChild();
        while (child != null) {
            IElementType elementType = PsiUtilCore.getElementType(child);
            if (elementType != DDocMetaElementTypes.DDOC_COMMENT_START
                && elementType != DDocMetaElementTypes.DDOC_COMMENT_END
                && elementType != DDocMetaElementTypes.DDOC_COMMENT_LEADING_ASTERISKS
                && elementType != TokenType.WHITE_SPACE) {
                array.add(child);
            }
            child = child.getNextSibling();
        }
        return PsiUtilCore.toPsiElementArray(array);
    }
}

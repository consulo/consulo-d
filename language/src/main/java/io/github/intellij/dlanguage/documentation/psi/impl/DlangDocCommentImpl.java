package io.github.intellij.dlanguage.documentation.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.LazyParseablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.documentation.psi.DlangDocComment;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_END;
import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_LEADING_ASTERISKS;
import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_START;

public class DlangDocCommentImpl extends LazyParseablePsiElement implements DlangDocComment {
    public DlangDocCommentImpl(IElementType type, @Nullable CharSequence text) {
        super(type, text);
    }

    @Override
    public IElementType getTokenType() {
        return getElementType();
    }

    // TODO introduce DPsiDocCommentOwner interface
    //  make some DPsiElement (the one that can have DDoc actually) implement it and return this new Interface instead
    @Override
    public @Nullable DNamedElement getOwner() {
        return PsiTreeUtil.getParentOfType(this, DNamedElement.class, /* strict */ true);
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        visitor.visitComment(this);
    }

    @Override
    public PsiElement[] sections() {
        List<PsiElement> array = new ArrayList<>();
        ASTNode child = getFirstChildNode();
        while (child != null) {
            IElementType type = child.getElementType();
            if (type != DDOC_COMMENT_START && type != DDOC_COMMENT_END && type != DDOC_COMMENT_LEADING_ASTERISKS) {
                array.add(child.getRequiredPsi());
            }
            child = child.getTreeNext();
        }
        return PsiUtilCore.toPsiElementArray(array);
    }
}

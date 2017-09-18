package net.masterthought.dlanguage.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import net.masterthought.dlanguage.psi.DLanguageAndExpression;
import net.masterthought.dlanguage.psi.DLanguageCmpExpression;
import net.masterthought.dlanguage.psi.DlangVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.masterthought.dlanguage.psi.DlangTypes.OP_AND;


public class DLanguageAndExpressionImpl extends ASTWrapperPsiElement implements DLanguageAndExpression {
    public DLanguageAndExpressionImpl(ASTNode node) {
        super(node);
    }

    public void accept(@NotNull DlangVisitor visitor) {
        visitor.visitAndExpression(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Nullable
    public DLanguageAndExpression getAndExpression() {
        return PsiTreeUtil.getChildOfType(this, DLanguageAndExpression.class);
    }

    @Nullable
    public DLanguageCmpExpression getCmpExpression() {
        return PsiTreeUtil.getChildOfType(this, DLanguageCmpExpression.class);
    }

    @Nullable
    public PsiElement getOP_AND() {
        return findChildByType(OP_AND);
    }

}

package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.stubs.DLanguageIfConditionStub;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Created by francis on 7/14/2017.
 */
public class DLanguageIfConditionImpl extends DNamedStubbedPsiElementBase<DLanguageIfConditionStub> implements DLanguageIfCondition {
    public DLanguageIfConditionImpl(final DLanguageIfConditionStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DLanguageIfConditionImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {visitor.visitDNamedElement(this);
        visitor.visitIfCondition(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public List<DLanguageAssignExpression> getAssignExpressions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageAssignExpression.class);
    }

    @Override
    public List<PsiElement> getOP_COMMAs() {
        return findChildrenByType(DlangTypes.OP_COMMA);
    }

    @Override
    public @Nullable PsiElement getKW_AUTO() {
        return findChildByType(DlangTypes.KW_AUTO);
    }

    @Override
    public @Nullable PsiElement getKW_SCOPE() {
        return findChildByType(DlangTypes.KW_SCOPE);
    }

    @Override
    public @Nullable DLanguageType getType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
    }

    @Override
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(DlangTypes.OP_EQ);
    }
}

package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateValueParameterStub;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.ID;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COLON;


public class DLanguageTemplateValueParameterImpl extends
    DNamedStubbedPsiElementBase<DLanguageTemplateValueParameterStub> implements DLanguageTemplateValueParameter {

    public DLanguageTemplateValueParameterImpl(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public DLanguageTemplateValueParameterImpl(DLanguageTemplateValueParameterStub stub,
        IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitTemplateValueParameter(this);
    }

    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) {
            accept((DlangVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    public @Nullable DLanguageType getType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
    }

    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    public @Nullable PsiElement getOP_COLON() {
        return findChildByType(OP_COLON);
    }

    public @Nullable DLanguageAssignExpression getAssignExpression() {
        return PsiTreeUtil.getChildOfType(this, DLanguageAssignExpression.class);
    }

    public @Nullable DLanguageTemplateValueParameterDefault getTemplateValueParameterDefault() {
        return PsiTreeUtil.getChildOfType(this, DLanguageTemplateValueParameterDefault.class);
    }

    @Override
    public DType getDType() {
        // TODO
        return new DUnknownType();
    }
}

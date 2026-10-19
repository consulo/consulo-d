package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageAssignExpression;
import io.github.intellij.dlanguage.psi.DLanguageTemplateAliasParameter;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateAliasParameterStub;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.github.intellij.dlanguage.psi.DlangTypes.*;


public class DLanguageTemplateAliasParameterImpl extends
    DNamedStubbedPsiElementBase<DLanguageTemplateAliasParameterStub> implements DLanguageTemplateAliasParameter {

    public DLanguageTemplateAliasParameterImpl(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public DLanguageTemplateAliasParameterImpl(DLanguageTemplateAliasParameterStub stub,
        IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitTemplateAliasParameter(this);
    }

    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) {
            accept((DlangVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    public @Nullable PsiElement getKW_ALIAS() {
        return findChildByType(KW_ALIAS);
    }

    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    public List<DLanguageType> getTypes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageType.class);
    }

    public List<DLanguageAssignExpression> getAssignExpressions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageAssignExpression.class);
    }

    public @Nullable PsiElement getOP_COLON() {
        return findChildByType(OP_COLON);
    }

    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(OP_EQ);
    }

    @Override
    public DType getDType() {
        // TODO
        return new DUnknownType();
    }

}

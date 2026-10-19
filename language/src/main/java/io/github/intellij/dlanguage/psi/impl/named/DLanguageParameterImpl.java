package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageParameterStub;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.github.intellij.dlanguage.psi.types.DTypesUtilKt.getDtypeWithSuffixes;

public class DLanguageParameterImpl extends
    DNamedStubbedPsiElementBase<DLanguageParameterStub> implements DLanguageParameter {

    public DLanguageParameterImpl(final DLanguageParameterStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageParameterImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitParameter(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable DLanguageAssignExpression getAssignExpression() {
        return PsiTreeUtil.getChildOfType(this, DLanguageAssignExpression.class);
    }

    @Override
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(DlangTypes.OP_EQ);
    }

    @Override
    public @Nullable PsiElement getKW_IMMUTABLE() {
        return findChildByType(DlangTypes.KW_IMMUTABLE);
    }
    @Override
    public @Nullable PsiElement getKW_SHARED() {
        return findChildByType(DlangTypes.KW_SHARED);
    }
    @Override
    public @Nullable PsiElement getKW_FINAL() {
        return findChildByType(DlangTypes.KW_FINAL);
    }
    @Override
    public @Nullable PsiElement getKW_CONST() {
        return findChildByType(DlangTypes.KW_CONST);
    }
    @Override
    public @Nullable PsiElement getKW_INOUT() {
        return findChildByType(DlangTypes.KW_INOUT);
    }
    @Override
    public @Nullable PsiElement getKW_IN() {
        return findChildByType(DlangTypes.KW_IN);
    }
    @Override
    public @Nullable PsiElement getKW_LAZY() {
        return findChildByType(DlangTypes.KW_LAZY);
    }
    @Override
    public @Nullable PsiElement getKW_OUT() {
        return findChildByType(DlangTypes.KW_OUT);
    }
    @Override
    public @Nullable PsiElement getKW_REF() {
        return findChildByType(DlangTypes.KW_REF);
    }
    @Override
    public @Nullable PsiElement getKW_SCOPE() {
        return findChildByType(DlangTypes.KW_SCOPE);
    }
    @Override
    public @Nullable PsiElement getKW_AUTO() {
        return findChildByType(DlangTypes.KW_AUTO);
    }
    @Override
    public @Nullable PsiElement getKW_RETURN() {
        return findChildByType(DlangTypes.KW_RETURN);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public List<DLanguageTypeSuffix> getTypeSuffixs() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageTypeSuffix.class);
    }

    @Override
    public @Nullable PsiElement getOP_TRIPLEDOT() {
        return findChildByType(DlangTypes.OP_TRIPLEDOT);
    }

    @Override
    public List<DLanguageParameterAttribute> getParameterAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageParameterAttribute.class);
    }

    @Override
    public @Nullable DLanguageType getType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
    }

//    public String getFullName() {
//        return DPsiImplUtil.getFullName(this);
//    }

    public @Nullable PsiElement getNameIdentifier() {
        if (getIdentifier() != null) {
            return getIdentifier();
        }
        if (getType() != null) {
            if (getType().getBasicType() != null) {
                if (getType().getBasicType().getQualifiedIdentifier() != null) {
                    return getType().getBasicType().getQualifiedIdentifier().getIdentifier();
                }
            }
        }
        return null;
    }

    @Contract(pure = true)
    public DType getDType() {
        var type = getType();
        if (type != null) {
            var dtype = type.getDType();
            return getDtypeWithSuffixes(dtype, getTypeSuffixs());
        }

        // Case for a parameter in function lambda expression
        return new DUnknownType(); // TODO
    }

    @Override
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, @Nullable PsiElement lastParent, PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }
}

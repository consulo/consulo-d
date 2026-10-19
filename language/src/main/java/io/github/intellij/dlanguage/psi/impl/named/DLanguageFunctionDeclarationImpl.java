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
import io.github.intellij.dlanguage.psi.interfaces.FunctionBody;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DTypesUtilKt;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageFunctionDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Created by francis on 7/14/2017.
 */
public class DLanguageFunctionDeclarationImpl extends
    DNamedStubbedPsiElementBase<DLanguageFunctionDeclarationStub> implements DLanguageFunctionDeclaration {

    public DLanguageFunctionDeclarationImpl(final DLanguageFunctionDeclarationStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DLanguageFunctionDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitDNamedElement(this);visitor.visitFunctionDeclaration(this);
    }

    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) {
            accept((DlangVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public @Nullable DLanguageBasicType getBasicType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageBasicType.class);
    }

    @Override
    public List<DLanguageTypeSuffix> getTypeSuffixes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageTypeSuffix.class);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public @Nullable DLanguageTemplateParameters getTemplateParameters() {
        return PsiTreeUtil.getChildOfType(this, DLanguageTemplateParameters.class);
    }

    @Override
    public @Nullable DLanguageParameters getParameters() {
        return PsiTreeUtil.getChildOfType(this, DLanguageParameters.class);
    }

    @Override
    public @Nullable DLanguageConstraint getConstraint() {
        return PsiTreeUtil.getChildOfType(this, DLanguageConstraint.class);
    }

    @Override
    public @Nullable FunctionBody getFunctionBody() {
        return PsiTreeUtil.getChildOfType(this, FunctionBody.class);
    }

    @Override
    public List<DLanguageMemberFunctionAttribute> getMemberFunctionAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageMemberFunctionAttribute.class);
    }

    @Override
    public List<DLanguageStorageClass> getStorageClasses() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageStorageClass.class);
    }

    @Override
    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this,processor, state, lastParent, place);
    }

    @Override
    public DType getReturnDType() {
        var basicType = getBasicType();
        if (basicType != null) {
            var type = basicType.getDType();
            return DTypesUtilKt.getDtypeWithSuffixes(type, getTypeSuffixes());
        }

        // At this point we can assume it is an auto function
        // can be "auto ref", "ref func()", …
        return new DUnknownType(); // TODO look in the method to get his return type (think to voldermore types case)
    }
}

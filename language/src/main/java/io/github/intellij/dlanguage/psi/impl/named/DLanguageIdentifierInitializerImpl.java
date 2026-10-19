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
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageIdentifierInitializerStub;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.types.DTypesUtilKt.getDtypeWithSuffixes;


public class DLanguageIdentifierInitializerImpl extends
    DNamedStubbedPsiElementBase<DLanguageIdentifierInitializerStub> implements DLanguageIdentifierInitializer {
    public DLanguageIdentifierInitializerImpl(final ASTNode node) {
        super(node);
    }

    public DLanguageIdentifierInitializerImpl(final DLanguageIdentifierInitializerStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);visitor.visitDeclarator(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(DlangTypes.OP_EQ);
    }

    public @Nullable DLanguageTemplateParameters getTemplateParameters() {
        return PsiTreeUtil.getChildOfType(this, DLanguageTemplateParameters.class);
    }

    public @Nullable DLanguageInitializer getInitializer() {
        return PsiTreeUtil.getChildOfType(this, DLanguageInitializer.class);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this,processor, state, lastParent, place);
    }

    @Override
    public DType getDType() {
        var parent = getParent();
        var basicType = ((DLanguageSpecifiedVariableDeclaration) parent).getBasicType();
        assert basicType != null;
        var type = basicType.getDType();
        return getDtypeWithSuffixes(type, ((DLanguageSpecifiedVariableDeclaration) parent).getTypeSuffixs());
    }
}

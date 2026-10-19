package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateDeclarationStub;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DlangTemplateDeclarationImpl extends
    DNamedStubbedPsiElementBase<DLanguageTemplateDeclarationStub> implements
    DLanguageTemplateDeclaration {

    public DlangTemplateDeclarationImpl(final DLanguageTemplateDeclarationStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DlangTemplateDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {visitor.visitDNamedElement(this);
        visitor.visitTemplateDeclaration(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable DLanguageConstraint getConstraint() {
        return PsiTreeUtil.getChildOfType(this, DLanguageConstraint.class);
    }

    @Override
    public @Nullable PsiElement getOP_BRACES_RIGHT() {
        return null;
    }

    @Override
    public @Nullable PsiElement getOP_BRACES_LEFT() {
        return null;
    }

    @Override
    public List<Declaration> getDeclarations() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, Declaration.class);
    }

    @Override
    public @Nullable PsiElement getKW_TEMPLATE() {
        return findChildByType(DlangTypes.KW_TEMPLATE);
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
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

    @Override
    public DType getDType() {
        return new DUnknownType(); // TODO
    }

}

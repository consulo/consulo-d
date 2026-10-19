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
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageStructDeclarationStub;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.KW_STRUCT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_SCOLON;

public class DlangStructDeclarationImpl extends
    DNamedStubbedPsiElementBase<DLanguageStructDeclarationStub> implements
    DLanguageStructDeclaration {

    public DlangStructDeclarationImpl(final DLanguageStructDeclarationStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DlangStructDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {visitor.visitDNamedElement(this);
        visitor.visitStructDeclaration(this);
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
    public @Nullable DLanguageStructBody getStructBody() {
        return PsiTreeUtil.getChildOfType(this, DLanguageStructBody.class);
    }

    @Override
    public @Nullable PsiElement getOP_SCOLON() {
        return findChildByType(OP_SCOLON);
    }

    @Override
    public @Nullable PsiElement getKW_STRUCT() {
        return findChildByType(KW_STRUCT);
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

    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

}

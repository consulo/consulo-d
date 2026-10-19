package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageEnumBody;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageEnumDeclarationStub;
import org.jspecify.annotations.Nullable;

public class DlangEnumDeclarationImpl extends
    DNamedStubbedPsiElementBase<DLanguageEnumDeclarationStub> implements
    DLanguageEnumDeclaration {

    public DlangEnumDeclarationImpl(final DLanguageEnumDeclarationStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DlangEnumDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);visitor.visitEnumDeclaration(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable DLanguageEnumBody getEnumBody() {
        return PsiTreeUtil.getChildOfType(this, DLanguageEnumBody.class);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public @Nullable PsiElement getOP_COLON() {
        return findChildByType(DlangTypes.OP_COLON);
    }

    @Override
    public @Nullable PsiElement getKW_ENUM() {
        return findChildByType(DlangTypes.KW_ENUM);
    }

    @Override
    public @Nullable DLanguageType getType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
    }

    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public boolean processDeclarations(PsiScopeProcessor processor,
                                       ResolveState state,
                                       @Nullable PsiElement lastParent,
                                       PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }
}

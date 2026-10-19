package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.interfaces.Statement;
import io.github.intellij.dlanguage.psi.DLanguageLabeledStatement;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageLabeledStatementStub;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.ID;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_COLON;

public class DLanguageLabeledStatementImpl extends DNamedStubbedPsiElementBase<DLanguageLabeledStatementStub> implements DLanguageLabeledStatement {

    public DLanguageLabeledStatementImpl(final DLanguageLabeledStatementStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageLabeledStatementImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitLabeledStatement(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    @Override
    public @Nullable PsiElement getOP_COLON() {
        return findChildByType(OP_COLON);
    }

    @Override
    public @Nullable Statement getStatement() {
        return PsiTreeUtil.getChildOfType(this, Statement.class);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

}

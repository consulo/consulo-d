package io.github.intellij.dlanguage.psi.impl.named;

import java.util.Objects;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.types.DAliasType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageAliasInitializerStub;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.github.intellij.dlanguage.psi.DlangTypes.ID;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_EQ;

public class DLanguageAliasInitializerImpl extends
    DNamedStubbedPsiElementBase<DLanguageAliasInitializerStub> implements DLanguageAliasInitializer {

    //todo doesn't cover all alias declarations possible
    public DLanguageAliasInitializerImpl(final DLanguageAliasInitializerStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageAliasInitializerImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {visitor.visitDNamedElement(this);
        visitor.visitAliasInitializer(this);
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
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(OP_EQ);
    }

    @Override
    public List<DLanguageStorageClass> getStorageClasss() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageStorageClass.class);
    }

    @Override
    public @Nullable DLanguageTemplateParameters getTemplateParameters() {
        return PsiTreeUtil.getChildOfType(this, DLanguageTemplateParameters.class);
    }

    @Override
    public @Nullable DLanguageType getType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
    }

    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

    public DType getDType() {
        if (getType() != null)
            return new DAliasType(Objects.requireNonNull(getIdentifier()).getText(), getType().getDType());
        throw new IllegalStateException("Unreachable statement");
    }

}

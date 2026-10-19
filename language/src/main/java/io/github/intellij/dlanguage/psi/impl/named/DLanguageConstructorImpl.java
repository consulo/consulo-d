package io.github.intellij.dlanguage.psi.impl.named;

import static io.github.intellij.dlanguage.psi.DlangTypes.KW_THIS;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_SCOLON;
import static io.github.intellij.dlanguage.utils.DUtil.getParentClassOrStructOrTemplateOrInterfaceOrUnion;

import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.interfaces.FunctionBody;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.impl.DLanguageParametersImpl;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageConstructorStub;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class DLanguageConstructorImpl extends
    DNamedStubbedPsiElementBase<DLanguageConstructorStub> implements DLanguageConstructor {

    public DLanguageConstructorImpl(final DLanguageConstructorStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageConstructorImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitConstructor(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable FunctionBody getFunctionBody() {
        return PsiTreeUtil.getChildOfType(this, FunctionBody.class);
    }

    @Override
    public @Nullable PsiElement getOP_SCOLON() {
        return findChildByType(OP_SCOLON);
    }

    @Override
    public @Nullable PsiElement getOP_PAR_RIGHT() {
        return findChildByType(OP_PAR_RIGHT);
    }

    @Override
    public @Nullable PsiElement getOP_PAR_LEFT() {
        return findChildByType(OP_PAR_LEFT);
    }

    @Override
    public @Nullable PsiElement getKW_THIS() {
        return findChildByType(KW_THIS);
    }

    @Override
    public List<DLanguageMemberFunctionAttribute> getMemberFunctionAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageMemberFunctionAttribute.class);
    }

    @Override
    public @Nullable DLanguageParameters getParameters() {
        return PsiTreeUtil.getChildOfType(this,DLanguageParameters.class);
    }

    @Override
    public @Nullable DLanguageTemplateParameters getTemplateParameters() {
        return PsiTreeUtil.getChildOfType(this,DLanguageTemplateParameters.class);
    }

    public @Nullable PsiElement getNameIdentifier() {
        final var parent = getParentClassOrStructOrTemplateOrInterfaceOrUnion(this);
        if (parent == null)
            return null;
        return parent.getNameIdentifier();
    }

    public ItemPresentation getPresentation() {
        return new DlangItemPresentation(getContainingFile()) {
            @Override
            public LocalizeValue getPresentableText() {
                String string = "";
                for (final PsiElement psiElement : getChildren()) {
                    if (psiElement instanceof DLanguageParametersImpl)
                        string += psiElement.getText();
                }
                return LocalizeValue.of(getName() + string);
            }
        };
    }

    public boolean processDeclarations(final PsiScopeProcessor processor, final ResolveState state, final @Nullable PsiElement lastParent, final PsiElement place) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

}

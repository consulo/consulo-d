package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.stubs.DLanguageModuleDeclarationStub;
import io.github.intellij.dlanguage.utils.DPsiUtilKt;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DlangModuleDeclarationImpl extends DNamedStubbedPsiElementBase<DLanguageModuleDeclarationStub> implements DLanguageModuleDeclaration {

    public DlangModuleDeclarationImpl(final DLanguageModuleDeclarationStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DlangModuleDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);
        visitor.visitModuleDeclaration(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    public @Nullable String getName() {
        final DLanguageModuleDeclarationStub greenStub = getGreenStub();
        if (greenStub != null) {
            return greenStub.getName();
        }
        if (getIdentifierChain() == null) {
            return getContainingFile().getName().replace(".d", "");
        }
        // Consulo: the import text (no whitespace or comments between the parts), as DlangPsiFileImpl reads it
        return DPsiUtilKt.getImportText(getIdentifierChain());
    }

    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifierChain();
    }

    public PsiElement setName(final String newName) {
        return this.replace(DElementFactory.createDLanguageModuleFromText(this.getProject(), newName));
    }

    public ItemPresentation getPresentation() {
        return new DlangItemPresentation(getContainingFile()) {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.ofNullable(getName());
            }
        };
    }

    public boolean hasAName() {
        return getIdentifierChain() != null;
    }

    @Override
    public @Nullable List<DLanguageAtAttribute> getAtAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageAtAttribute.class);
    }
    @Override
    public @Nullable DLanguageDeprecated getDeprecated() {
        return PsiTreeUtil.getChildOfType(this, DLanguageDeprecated.class);
    }

    @Override
    public @Nullable PsiElement getKW_MODULE() {
        return findChildByType(DlangTypes.KW_MODULE);
    }

    @Override
    public @Nullable DLanguageIdentifierChain getIdentifierChain() {
        return PsiTreeUtil.getChildOfType(this, DLanguageIdentifierChain.class);
    }

    @Override
    public @Nullable PsiElement getOP_SCOLON() {
        return findChildByType(DlangTypes.OP_SCOLON);
    }
}

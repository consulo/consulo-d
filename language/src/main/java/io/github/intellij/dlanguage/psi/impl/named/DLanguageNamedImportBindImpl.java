package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.stubs.DLanguageNamedImportBindStub;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.OP_EQ;
import static io.github.intellij.dlanguage.psi.DlangTypes.ID;


public class DLanguageNamedImportBindImpl extends DNamedStubbedPsiElementBase<DLanguageNamedImportBindStub> implements DLanguageNamedImportBind {

    public DLanguageNamedImportBindImpl(final DLanguageNamedImportBindStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageNamedImportBindImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);visitor.visitNamedImportBind(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }


    @Override
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(OP_EQ);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

}

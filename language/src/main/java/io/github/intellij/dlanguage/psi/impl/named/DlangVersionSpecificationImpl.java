

package io.github.intellij.dlanguage.psi.impl.named;

import static io.github.intellij.dlanguage.psi.DlangTypes.*;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.stubs.DLanguageVersionSpecificationStub;
import org.jspecify.annotations.Nullable;


public class DlangVersionSpecificationImpl extends
    DNamedStubbedPsiElementBase<DLanguageVersionSpecificationStub> implements
    DLanguageVersionSpecification {

    public DlangVersionSpecificationImpl(
        final DLanguageVersionSpecificationStub stub,
        final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DlangVersionSpecificationImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitVersionSpecification(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) {
            accept((DlangVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    public @Nullable PsiElement getKW_VERSION() {
        return findChildByType(KW_VERSION);
    }

    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(OP_EQ);
    }

    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    public @Nullable PsiElement getOP_PAR_RIGHT() {
        return findChildByType(OP_PAR_RIGHT);
    }

    public @Nullable PsiElement getOP_PAR_LEFT() {
        return findChildByType(OP_PAR_LEFT);
    }

    public @Nullable PsiElement getINTEGER_LITERAL() {
        return findChildByType(INTEGER_LITERAL);
    }

    public @Nullable PsiElement getOP_SCOLON() {
        return findChildByType(OP_SCOLON);
    }

}

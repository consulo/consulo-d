

package io.github.intellij.dlanguage.psi.impl.named;

import static io.github.intellij.dlanguage.psi.DlangTypes.INTEGER_LITERAL;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_VERSION;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_EQ;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_SCOLON;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import io.github.intellij.dlanguage.psi.named.DlangIdentifier;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.stubs.VersionSpecificationStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DLanguageVersionSpecificationImpl extends
    DNamedStubbedPsiElementBase<VersionSpecificationStub> implements
    DLanguageVersionSpecification {

    public DLanguageVersionSpecificationImpl(
        @NotNull final VersionSpecificationStub stub,
        final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DLanguageVersionSpecificationImpl(final ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public DlangIdentifier getNameIdentifier() {
        return getIdentifier();
    }

    public void accept(@NotNull final DlangVisitor visitor) {
        visitor.visitVersionSpecification(this);
    }

    public void accept(@NotNull final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) {
            accept((DlangVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    @Nullable
    public PsiElement getKW_VERSION() {
        return findChildByType(KW_VERSION);
    }

    @Nullable
    public PsiElement getOP_EQ() {
        return findChildByType(OP_EQ);
    }

    @Nullable
    public DlangIdentifier getIdentifier() {
        return PsiTreeUtil.getChildOfType(this, DlangIdentifier.class);
    }

    @Nullable
    public PsiElement getOP_PAR_RIGHT() {
        return findChildByType(OP_PAR_RIGHT);
    }

    @Nullable
    public PsiElement getOP_PAR_LEFT() {
        return findChildByType(OP_PAR_LEFT);
    }

    @Nullable
    public PsiElement getINTEGER_LITERAL() {
        return findChildByType(INTEGER_LITERAL);
    }

    @Nullable
    public PsiElement getOP_SCOLON() {
        return findChildByType(OP_SCOLON);
    }

}

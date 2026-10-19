package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTupleParameter;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateTupleParameterStub;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.ID;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_TRIPLEDOT;


public class DLanguageTemplateTupleParameterImpl extends
    DNamedStubbedPsiElementBase<DLanguageTemplateTupleParameterStub> implements DLanguageTemplateTupleParameter {

    public DLanguageTemplateTupleParameterImpl(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    public DLanguageTemplateTupleParameterImpl(DLanguageTemplateTupleParameterStub stub,
        IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitTemplateTupleParameter(this);
    }

    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) {
            accept((DlangVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    public @Nullable PsiElement getIdentifier() {
        return findChildByType(ID);
    }

    public @Nullable PsiElement getOP_TRIPLEDOT() {
        return findChildByType(OP_TRIPLEDOT);
    }

    @Override
    public DType getDType() {
        // TODO
        return new DUnknownType();
    }
}

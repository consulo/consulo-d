package io.github.intellij.dlanguage.psi.impl;

import static io.github.intellij.dlanguage.psi.DlangTypes.KW_THIS;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_LEFT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_PAR_RIGHT;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_SCOLON;
import static io.github.intellij.dlanguage.psi.DlangTypes.OP_TILDA;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageMemberFunctionAttribute;
import io.github.intellij.dlanguage.psi.interfaces.FunctionBody;
import io.github.intellij.dlanguage.psi.named.DLanguageDestructor;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.stubs.DLanguageDestructorStub;
import java.util.List;
import org.jspecify.annotations.Nullable;


public class DLanguageDestructorImpl extends DStubbedPsiElementBase<DLanguageDestructorStub> implements
        DLanguageDestructor {
    public DLanguageDestructorImpl(final DLanguageDestructorStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DLanguageDestructorImpl(final ASTNode node) {
        super(node);

    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDestructor(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    public @Nullable FunctionBody getFunctionBody() {
        return PsiTreeUtil.getChildOfType(this, FunctionBody.class);
    }

    public @Nullable PsiElement getOP_SCOLON() {
        return findChildByType(OP_SCOLON);
    }

    public @Nullable PsiElement getOP_PAR_RIGHT() {
        return findChildByType(OP_PAR_RIGHT);
    }

    public @Nullable PsiElement getOP_PAR_LEFT() {
        return findChildByType(OP_PAR_LEFT);
    }

    public @Nullable PsiElement getKW_THIS() {
        return findChildByType(KW_THIS);
    }

    public @Nullable PsiElement getOP_TILDA() {
        return findChildByType(OP_TILDA);
    }

    public List<DLanguageMemberFunctionAttribute> getMemberFunctionAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageMemberFunctionAttribute.class);
    }
}

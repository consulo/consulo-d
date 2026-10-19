package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.*;
import io.github.intellij.dlanguage.psi.impl.DNamedStubbedPsiElementBase;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.stubs.DLanguageEnumMemberStub;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DLanguageEnumMemberImpl extends
    DNamedStubbedPsiElementBase<DLanguageEnumMemberStub> implements DLanguageEnumMember {

    public DLanguageEnumMemberImpl(final DLanguageEnumMemberStub stub, final IStubElementType type) {
        super(stub, type);
    }

    public DLanguageEnumMemberImpl(final ASTNode node) {
        super(node);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitDNamedElement(this);visitor.visitEnumMember(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }


    @Override
    public List<DLanguageEnumMemberAttribute> getEnumMemberAttributes() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DLanguageEnumMemberAttribute.class);
    }

    @Override
    public @Nullable Expression getExpression() {
        return PsiTreeUtil.getChildOfType(this, Expression.class);
    }

    @Override
    public @Nullable PsiElement getIdentifier() {
        return findChildByType(DlangTypes.ID);
    }

    @Override
    public @Nullable PsiElement getOP_EQ() {
        return findChildByType(DlangTypes.OP_EQ);
    }

    @Override
    public @Nullable DLanguageType getType() {
        return PsiTreeUtil.getChildOfType(this, DLanguageType.class);
    }

//    public String getFullName() {
//        return DPsiImplUtil.getFullName(this);
//    }

    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public DType getDType() {
        // Only anonymous members has this type set
        if (getType() != null) {
            return getType().getDType();
        }

        if (getParent().getParent() instanceof DLanguageEnumDeclaration enumDeclaration) {
            return enumDeclaration.getDType();
        }

        if (getParent() instanceof DLanguageAnonymousEnumDeclaration anonymousEnumDeclaration && anonymousEnumDeclaration.getType() != null) {
            return anonymousEnumDeclaration.getType().getDType();
        }

        if (getExpression() != null) {
            var retType = getExpression().getDType();
            if (retType != null)
                return retType;
            return new DUnknownType();
        }

        // for anonymous enums, take the type of the previous member if this member has a type declared
        PsiElement element = getPrevSibling();
        while(element != null) {
            if (element instanceof DLanguageEnumMember enumMember) {
                if (enumMember.getType() != null)
                    return enumMember.getType().getDType();
                break;
            }
            element = element.getPrevSibling();
        }

        if (getParent() instanceof DLanguageAnonymousEnumDeclaration anonymousEnumDeclaration) {
            return anonymousEnumDeclaration.getDType();
        }

        assert false : "Unexpected/Unimplemented case of enum member";
        return new DUnknownType();
    }
}

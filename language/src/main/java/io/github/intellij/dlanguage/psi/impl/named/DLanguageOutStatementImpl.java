package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.named.DLanguageOutStatement;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.DUnknownType;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static io.github.intellij.dlanguage.psi.DlangTypes.*;


public class DLanguageOutStatementImpl extends ASTWrapperPsiElement implements
    DLanguageOutStatement {

    public DLanguageOutStatementImpl(ASTNode node) {
        super(node);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitOutStatement(this);
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

    public @Nullable DLanguageBlockStatement getBlockStatement() {
        return PsiTreeUtil.getChildOfType(this, DLanguageBlockStatement.class);
    }

    public @Nullable PsiElement getKW_OUT() {
        return findChildByType(KW_OUT);
    }

    public @Nullable PsiElement getOP_PAR_LEFT() {
        return findChildByType(OP_PAR_LEFT);
    }

    public @Nullable PsiElement getOP_PAR_RIGHT() {
        return findChildByType(OP_PAR_RIGHT);
    }

    @Override
    public @Nullable DAttributes getAttributes() {
        return null;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return true;
    }

    @Override
    public DAttributesFinder.Visibility visibility() {
        return DAttributesFinder.Visibility.PRIVATE;
    }

    @Override
    public boolean isProperty() {
        return false;
    }

    @Override
    public boolean isNoGC() {
        return false;
    }

    @Override
    public boolean isExtern() {
        return false;
    }

    @Override
    public boolean isPure() {
        return false;
    }

    @Override
    public boolean isNothrow() {
        return false;
    }

    @Override
    public boolean isConst() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        if (getNameIdentifier() == null) {
            throw new IncorrectOperationException("Cannot rename. Identifier was Null");
        }
        getNameIdentifier().replace(
            Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(
                getProject(),
                name
            ))
        );
        return this;
    }

    @Override
    public DType getDType() {
        // TODO the type is the return type of it’s parent class
        return new DUnknownType();
    }
}

package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.stubs.DLanguageUnittestStub;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.DLanguageUnittest;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import org.jspecify.annotations.Nullable;


public class DlangUnittestImpl extends DStubbedPsiElementBase<DLanguageUnittestStub> implements DLanguageUnittest {

    public DlangUnittestImpl(final ASTNode node) {
        super(node);
    }

    public DlangUnittestImpl(final DLanguageUnittestStub stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public void accept(final DlangVisitor visitor) {
        visitor.visitUnittest(this);
    }

    public void accept(final PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor) accept((DlangVisitor) visitor);
        else super.accept(visitor);
    }

    public @Nullable DLanguageUnittest getUnittest() {
        return PsiTreeUtil.getChildOfType(this, DLanguageUnittest.class);
    }

    public @Nullable DLanguageBlockStatement getBlockStatement() {
        return PsiTreeUtil.getChildOfType(this, DLanguageBlockStatement.class);
    }
}

package io.github.intellij.dlanguage.psi.impl;

import consulo.language.impl.psi.LazyParseablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageBlockStatement;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.interfaces.Statement;
import io.github.intellij.dlanguage.resolve.ScopeProcessorImpl;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DLanguageBlockStatementImpl extends LazyParseablePsiElement implements DLanguageBlockStatement {
    public DLanguageBlockStatementImpl(@Nullable CharSequence text) {
        super(DlangTypes.BLOCK_STATEMENT, text);
    }

    public void accept(DlangVisitor visitor) {
        visitor.visitBlockStatement(this);
    }

    @Override
    public void accept(PsiElementVisitor visitor) {
        if (visitor instanceof DlangVisitor dlangVisitor) {
            accept(dlangVisitor);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public List<Statement> getStatements() {
        List<Statement> statements = new ArrayList<>();
        statements.add(PsiTreeUtil.getChildOfType(this, Statement.class));
        return statements;
    }

    @Override
    public @Nullable PsiElement getOP_BRACES_RIGHT() {
        return getFirstChild();
    }

    @Override
    public @Nullable PsiElement getOP_BRACES_LEFT() {
        return getLastChild();
    }

    @Override
    public boolean processDeclarations(
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        return ScopeProcessorImpl.INSTANCE.processDeclarations(this, processor, state, lastParent, place);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getElementType() + ")";
    }
}

package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageBreakStatement;
import io.github.intellij.dlanguage.psi.references.LabelReference;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageBreakStatementImplMixin extends ASTWrapperPsiElement implements DLanguageBreakStatement {
    public DLanguageBreakStatementImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiReference getReference() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) {
            return null;
        }
        return new LabelReference(this, identifier.getTextRangeInParent());
    }
}

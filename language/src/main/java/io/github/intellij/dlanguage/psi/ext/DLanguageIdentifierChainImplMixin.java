package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.references.PackageReference;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageIdentifierChainImplMixin extends ASTWrapperPsiElement implements DLanguageIdentifierChain {
    public DLanguageIdentifierChainImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiReference getReference() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) {
            return null;
        }
        return new PackageReference(this, identifier.getTextRangeInParent());
    }
}

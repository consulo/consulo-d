package io.github.intellij.dlanguage.psi.ext;

import consulo.d.language.resolve.DResolveCaching;
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

    /**
     * Consulo: one reference object per element until the file changes, see {@link DResolveCaching}.
     */
    @Override
    public @Nullable PsiReference getReference() {
        return DResolveCaching.reference(this, this::createReference);
    }

    private @Nullable PsiReference createReference() {
        PsiElement identifier = getIdentifier();
        if (identifier == null) {
            return null;
        }
        return new PackageReference(this, identifier.getTextRangeInParent());
    }
}

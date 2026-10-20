package io.github.intellij.dlanguage.psi.ext;

import consulo.d.language.resolve.DResolveCaching;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageImportBind;
import io.github.intellij.dlanguage.psi.references.ImportBindReference;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageImportBindImplMixin extends ASTWrapperPsiElement implements DLanguageImportBind {
    public DLanguageImportBindImplMixin(ASTNode node) {
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
        PsiElement referenceElement = getIdentifier();
        if (referenceElement == null) {
            return null;
        }
        TextRange range = referenceElement.getTextRangeInParent();
        return new ImportBindReference(this, range);
    }
}

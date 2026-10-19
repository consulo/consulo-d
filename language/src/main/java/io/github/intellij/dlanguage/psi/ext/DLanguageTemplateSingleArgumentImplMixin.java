package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.psi.DLanguageTemplateSingleArgument;
import io.github.intellij.dlanguage.psi.references.TypeInstanceReference;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageTemplateSingleArgumentImplMixin extends ASTWrapperPsiElement implements DLanguageTemplateSingleArgument {
    public DLanguageTemplateSingleArgumentImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiReference getReference() {
        PsiElement identifier = getIdentifier();
        if (identifier != null) {
            return new TypeInstanceReference(this, identifier.getTextRangeInParent());
        }
        return null;
    }
}

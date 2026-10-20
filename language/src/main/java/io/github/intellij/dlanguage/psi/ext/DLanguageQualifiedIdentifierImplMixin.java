package io.github.intellij.dlanguage.psi.ext;

import consulo.d.language.resolve.DResolveCaching;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.DLanguageTypeSuffix;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.references.AliasValueReference;
import io.github.intellij.dlanguage.psi.references.TypeReference;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageQualifiedIdentifierImplMixin extends ASTWrapperPsiElement implements DLanguageQualifiedIdentifier {
    public DLanguageQualifiedIdentifierImplMixin(ASTNode node) {
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
        PsiElement referenceElement;
        TextRange range;
        PsiElement identifier = getIdentifier();
        DLanguageTemplateInstance templateInstance = getTemplateInstance();
        PsiElement templateInstanceIdentifier = templateInstance != null ? templateInstance.getIdentifier() : null;
        if (identifier != null) {
            referenceElement = identifier;
            range = referenceElement.getTextRangeInParent();
        }
        else if (templateInstance != null && templateInstanceIdentifier != null) {
            referenceElement = templateInstanceIdentifier;
            range = referenceElement.getTextRangeInParent().shiftRight(templateInstance.getStartOffsetInParent());
        }
        else {
            return null;
        }
        if (PsiTreeUtil.getParentOfType(this, DLanguageAliasInitializer.class, true, Declaration.class, DLanguageParameter.class) != null) {
            return new AliasValueReference(this, range, getQualifiedIdentifier(), referenceElement.getText());
        }
        boolean inTypeSuffix =
            PsiTreeUtil.getParentOfType(this, DLanguageTypeSuffix.class, true, Declaration.class, DLanguageParameter.class) != null;
        return new TypeReference(this, range, getQualifiedIdentifier(), referenceElement.getText(), inTypeSuffix);
    }
}

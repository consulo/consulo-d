package io.github.intellij.dlanguage.psi.references;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.DLanguageTemplateSingleArgument;
import io.github.intellij.dlanguage.psi.TypeResolveUtil;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class TypeInstanceReference extends PsiReferenceBase<DLanguageTemplateSingleArgument> implements PsiQualifiedReference {
    public TypeInstanceReference(DLanguageTemplateSingleArgument element, TextRange textRange) {
        super(element, textRange);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return TypeResolveUtil.INSTANCE.resolveType(this, false, false, false);
    }

    @Override
    public @Nullable PsiElement getQualifier() {
        return null;
    }

    @Override
    public String getReferenceName() {
        return Objects.requireNonNull(getElement().getIdentifier()).getText();
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement identifier = myElement.getIdentifier();
        if (identifier != null) {
            PsiElement element = Objects.requireNonNull(
                DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName)
            );
            identifier.replace(element);
        }
        else {
            throw new IncorrectOperationException();
        }
        return myElement;
    }

}

package io.github.intellij.dlanguage.psi.references;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.resolve.processor.GenericProcessor;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class AliasValueReference extends PsiReferenceBase<DLanguageQualifiedIdentifier> implements PsiQualifiedReference {
    private final @Nullable DLanguageQualifiedIdentifier myQualifiedIdentifier;
    private String myReferenceName;

    public AliasValueReference(
        DLanguageQualifiedIdentifier element,
        TextRange textRange,
        @Nullable DLanguageQualifiedIdentifier qualifiedIdentifier,
        String referenceName
    ) {
        super(element, textRange);
        myQualifiedIdentifier = qualifiedIdentifier;
        myReferenceName = referenceName;
    }

    @Override
    public @Nullable PsiElement resolve() {
        GenericProcessor processor = new GenericProcessor(myReferenceName, myElement);
        PsiScopesUtil.INSTANCE.resolveAndWalk(processor, this, null);
        ResolveResult[] result = processor.getResult();
        if (result.length == 1) {
            return (DNamedElement) result[0].getElement();
        }
        return null;
    }

    @Override
    public @Nullable PsiElement getQualifier() {
        return myQualifiedIdentifier;
    }

    @Override
    public String getReferenceName() {
        return myReferenceName;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement identifier = myElement.getIdentifier();
        DLanguageTemplateInstance templateInstance = myElement.getTemplateInstance();
        PsiElement templateInstanceIdentifier = templateInstance != null ? templateInstance.getIdentifier() : null;
        if (identifier != null) {
            PsiElement element = Objects.requireNonNull(
                DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName)
            );
            identifier.replace(element);
        }
        else if (templateInstanceIdentifier != null) {
            templateInstanceIdentifier.replace(
                Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName))
            );
        }
        else {
            throw new IncorrectOperationException();
        }
        myReferenceName = newElementName;
        return myElement;
    }
}

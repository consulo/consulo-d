package io.github.intellij.dlanguage.psi.references;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiUtilCore;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.TypeResolveUtil;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class TypeReference extends PsiReferenceBase<DLanguageQualifiedIdentifier> implements PsiQualifiedReference {
    private final @Nullable DLanguageQualifiedIdentifier myQualifiedIdentifier;
    private String myReferenceName;
    private final boolean myInTypeSuffix;

    public TypeReference(
        DLanguageQualifiedIdentifier element,
        TextRange textRange,
        @Nullable DLanguageQualifiedIdentifier qualifiedIdentifier,
        String referenceName,
        boolean inTypeSuffix
    ) {
        super(element, textRange);
        myQualifiedIdentifier = qualifiedIdentifier;
        myReferenceName = referenceName;
        myInTypeSuffix = inTypeSuffix;
    }

    @Override
    public @Nullable PsiElement resolve() {
        return TypeResolveUtil.INSTANCE.resolveType(
            this,
            myElement.getTemplateInstance() != null,
            myInTypeSuffix,
            isModuleScopeOperator(getElement())
        );
    }

    @Override
    public @Nullable PsiElement getQualifier() {
        return myQualifiedIdentifier;
    }

    @Override
    public String getReferenceName() {
        return myReferenceName;
    }

    public boolean isModuleScopeOperator(DLanguageQualifiedIdentifier e) {
        if (e.getQualifiedIdentifier() != null) {
            // not the innermost (most-left) element
            return false;
        }

        DLanguageQualifiedIdentifier current = e;
        PsiElement parent = e.getParent();
        while (parent instanceof DLanguageQualifiedIdentifier parentIdentifier) {
            if (parentIdentifier.getFirstChild() != current) {
                return false;
            }
            current = parentIdentifier;
            parent = current.getParent();
        }
        // `typeof.qualifiedIdentifier` case exists, so DOT can be here but not the first child
        return parent instanceof DLanguageBasicType && PsiUtilCore.getElementType(parent.getFirstChild()) == DlangTypes.OP_DOT;
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

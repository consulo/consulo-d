package io.github.intellij.dlanguage.psi.references;

import consulo.d.language.resolve.DResolveCaching;
import consulo.language.psi.resolve.ResolveCache;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.DLanguageReferenceExpression;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.resolve.processor.GenericProcessor;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class GenericExpressionElementReference extends PsiReferenceBase<DLanguageReferenceExpression> implements PsiQualifiedReference {
    private final @Nullable Expression myQualifier;
    private final String myReferenceName;

    public GenericExpressionElementReference(
        DLanguageReferenceExpression element,
        TextRange textRange,
        @Nullable Expression qualifier,
        String referenceName
    ) {
        super(element, textRange);
        myQualifier = qualifier;
        myReferenceName = referenceName;
    }

    private static final ResolveCache.AbstractResolver<GenericExpressionElementReference, PsiElement> RESOLVER =
        (reference, incompleteCode) -> reference.resolveInner();

    /**
     * Consulo: cached, see {@link DResolveCaching}.
     */
    @Override
    public @Nullable PsiElement resolve() {
        return DResolveCaching.resolve(this, RESOLVER);
    }

    private @Nullable PsiElement resolveInner() {
        GenericProcessor processor = new GenericProcessor(myReferenceName, myElement);
        if (isModuleScopeOperator(getElement())) {
            PsiFile containingFile = Objects.requireNonNull(getElement().getContainingFile());
            containingFile.processDeclarations(processor, ResolveState.initial(), containingFile, getElement());
        }
        else {
            PsiScopesUtil.INSTANCE.resolveAndWalk(processor, this, null);
        }
        ResolveResult[] result = processor.getResult();
        if (result.length == 1) {
            return (DNamedElement) result[0].getElement();
        }
        return null;
    }

    public boolean isModuleScopeOperator(PsiElement e) {
        return e instanceof DLanguageReferenceExpression referenceExpression
            && referenceExpression.getOP_DOT() != null
            && referenceExpression.getExpression() == null;
    }

    @Override
    public @Nullable PsiElement getQualifier() {
        return myQualifier;
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
            identifier.replace(
                Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName))
            );
        }
        else if (templateInstanceIdentifier != null) {
            templateInstanceIdentifier.replace(
                Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName))
            );
        }
        else {
            throw new IncorrectOperationException();
        }
        return myElement;
    }
}

package io.github.intellij.dlanguage.psi.references;

import consulo.d.language.resolve.DResolveCaching;
import consulo.language.psi.resolve.ResolveCache;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.LabelQualifier;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.resolve.processor.LabelProcessor;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class LabelReference extends PsiReferenceBase<LabelQualifier> {
    public LabelReference(LabelQualifier element, TextRange textRange) {
        super(element, textRange);
    }

    private static final ResolveCache.AbstractResolver<LabelReference, PsiElement> RESOLVER =
        (reference, incompleteCode) -> reference.resolveInner();

    /**
     * Consulo: cached, see {@link DResolveCaching}.
     */
    @Override
    public @Nullable PsiElement resolve() {
        return DResolveCaching.resolve(this, RESOLVER);
    }

    private @Nullable PsiElement resolveInner() {
        LabelProcessor processor = new LabelProcessor(Objects.requireNonNull(getElement().getIdentifier()).getText());
        PsiElement current = getElement().getParent();
        while ((!(current instanceof DLanguageFunctionDeclaration) || !(current instanceof DLanguageTemplateDeclaration))
            && current != null) {
            PsiScopesUtil.INSTANCE.walkChildrenScopes(current, processor, ResolveState.initial(), null, getElement());
            ResolveResult[] result = processor.getResult();
            if (result.length == 1) {
                return (DNamedElement) result[0].getElement();
            }
            current = current.getParent();
        }
        return null;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement element = Objects.requireNonNull(
            DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName)
        );
        Objects.requireNonNull(myElement.getIdentifier()).replace(element);
        return element;
    }
}

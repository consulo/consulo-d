package io.github.intellij.dlanguage.psi.references;

import consulo.d.language.resolve.DResolveCaching;
import consulo.language.psi.resolve.ResolveCache;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import io.github.intellij.dlanguage.psi.DLanguageImportBind;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.resolve.processor.GenericProcessor;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ImportBindReference extends PsiReferenceBase<DLanguageImportBind> {
    public ImportBindReference(DLanguageImportBind element, TextRange textRange) {
        super(element, textRange);
    }

    private static final ResolveCache.AbstractResolver<ImportBindReference, PsiElement> RESOLVER =
        (reference, incompleteCode) -> reference.resolveInner();

    /**
     * Consulo: cached, see {@link DResolveCaching}.
     */
    @Override
    public @Nullable PsiElement resolve() {
        return DResolveCaching.resolve(this, RESOLVER);
    }

    private @Nullable PsiElement resolveInner() {
        GenericProcessor processor = new GenericProcessor(Objects.requireNonNull(getElement().getIdentifier()).getText(), myElement);
        PsiScopesUtil.INSTANCE.treeWalkUp(
            processor,
            getElement(),
            PsiTreeUtil.getParentOfType(getElement(), DLanguageImportDeclaration.class, true)
        );
        ResolveResult[] result = processor.getResult();
        if (result.length == 1) {
            return (DNamedElement) result[0].getElement();
        }
        return null;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement identifier = myElement.getIdentifier();
        if (identifier == null) {
            throw new IncorrectOperationException();
        }
        identifier.replace(
            Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newElementName))
        );
        return myElement;
    }
}

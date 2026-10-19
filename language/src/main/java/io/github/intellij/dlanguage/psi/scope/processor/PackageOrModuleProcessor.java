package io.github.intellij.dlanguage.psi.scope.processor;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.psi.scope.ElementDeclarationHint;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.intellij.dlanguage.utils.DPsiUtilKt.getImportText;

public class PackageOrModuleProcessor implements PsiScopeProcessor, ElementDeclarationHint {
    private final PsiScopeProcessor myDelegate;
    private final @Nullable DLanguagePackage myParentPackage;

    public PackageOrModuleProcessor(PsiScopeProcessor delegate, @Nullable DLanguagePackage parentPackage) {
        myDelegate = delegate;
        myParentPackage = parentPackage;
    }

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (element instanceof DLanguageImportDeclaration importDeclaration) {
            List<DLanguageIdentifierChain> packages = new ArrayList<>();
            for (DLanguageSingleImport singleImport : importDeclaration.getSingleImports()) {
                if (singleImport.getIdentifier() != null) {
                    continue;
                }
                DLanguageIdentifierChain id = singleImport.getIdentifierChain();
                if (id == null) {
                    continue;
                }
                DLanguageIdentifierChain parentChain = id.getIdentifierChain();
                while (parentChain != null
                    && (myParentPackage == null || !Objects.equals(getImportText(parentChain), myParentPackage.getQualifiedName()))) {
                    id = parentChain;
                    parentChain = id.getIdentifierChain();
                }
                packages.add(id);
            }
            for (DLanguageIdentifierChain aPackage : packages) {
                PsiReference reference = aPackage.getReference();
                PsiElement resolvedPackage = reference != null ? reference.resolve() : null;
                if (resolvedPackage == null) {
                    continue;
                }
                if (!myDelegate.execute(resolvedPackage, state)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldProcess(ElementDeclarationHint.DeclarationKind kind) {
        return kind != ElementDeclarationHint.DeclarationKind.IMPORT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getHint(Key<T> hintKey) {
        if (ElementDeclarationHint.KEY.equals(hintKey)) {
            return (T) this;
        }
        return PsiScopeProcessor.super.getHint(hintKey);
    }
}

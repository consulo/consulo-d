package io.github.intellij.dlanguage.psi.scope.processor;

import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.psi.DLanguageAttribute;
import io.github.intellij.dlanguage.psi.DLanguageAttributeSpecifier;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.interfaces.DVisibility;
import io.github.intellij.dlanguage.psi.scope.ElementDeclarationHint;
import org.jspecify.annotations.Nullable;

public class DVisibilityProcessor implements PsiScopeProcessor, ElementDeclarationHint {
    private @Nullable DVisibility myResult = null;

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (element instanceof DLanguageAttributeSpecifier attributeSpecifier) {
            DLanguageAttribute attribute = attributeSpecifier.getAttribute();
            if (attribute != null) {
                if (attribute.getKW_PUBLIC() != null) {
                    myResult = DVisibility.Public.INSTANCE;
                }
                else if (attribute.getKW_PRIVATE() != null) {
                    myResult = DVisibility.Private.INSTANCE;
                }
                else if (attribute.getKW_PROTECTED() != null) {
                    myResult = DVisibility.Protected.INSTANCE;
                }
                else if (attribute.getKW_EXPORT() != null) {
                    myResult = DVisibility.Export.INSTANCE;
                }
                else if (attribute.getKW_PACKAGE() != null) {
                    DLanguageIdentifierChain identifierChain = attribute.getIdentifierChain();
                    myResult = new DVisibility.Package(identifierChain != null ? identifierChain.getText() : null);
                }
                else {
                    myResult = null;
                }
            }
            if (myResult != null) {
                return false;
            }
        }
        return true;
    }

    public @Nullable DVisibility getVisibility() {
        return myResult;
    }

    @Override
    public boolean shouldProcess(ElementDeclarationHint.DeclarationKind kind) {
        return kind != ElementDeclarationHint.DeclarationKind.IMPORT
            && kind != ElementDeclarationHint.DeclarationKind.CLASS
            && kind != ElementDeclarationHint.DeclarationKind.INTERFACE;
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

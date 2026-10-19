package io.github.intellij.dlanguage.psi.interfaces;

import consulo.language.psi.PsiElement;

// TODO move this comment to the relevant place (in the visibilityLookup)
// Note: local symbols (declaration in DeclarationStatements) cannot have an attribute, hence his visibility is private
// TODO add this interface to more declarations that have visibility

/**
 * Port of the {@code DVisibilityModifier} interface declared in upstream {@code DVisibility.kt}.
 */
public interface DVisibilityModifier extends PsiElement {
    DVisibility visibility();
}

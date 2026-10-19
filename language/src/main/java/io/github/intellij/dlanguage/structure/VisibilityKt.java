package io.github.intellij.dlanguage.structure;

import io.github.intellij.dlanguage.psi.DLanguageAttribute;

/**
 * Top-level declarations of upstream's {@code Visibility.kt}.
 */
public final class VisibilityKt {
    private VisibilityKt() {
    }

    public static Visibility fromPsiAttribute(DLanguageAttribute attribute) {
        if (attribute.getKW_PUBLIC() != null) {
            return Visibility.PUBLIC;
        }

        if (attribute.getKW_PRIVATE() != null) {
            return Visibility.PRIVATE;
        }

        if (attribute.getKW_PROTECTED() != null) {
            return Visibility.PROTECTED;
        }

        if (attribute.getKW_PACKAGE() != null) {
            return Visibility.PACKAGE;
        }

        return Visibility.NONE;
    }
}

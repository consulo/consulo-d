package io.github.intellij.dlanguage.utils;

import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;

/**
 * Top-level functions of upstream {@code DPsiUtil.kt}.
 */
public final class DPsiUtilKt {
    private DPsiUtilKt() {
    }

    public static String getImportText(DLanguageIdentifierChain chain) {
        PsiElement identifier = chain.getIdentifier();
        if (identifier == null) {
            return "";
        }
        String text = identifier.getText();
        DLanguageIdentifierChain identifierChain = chain.getIdentifierChain();
        if (identifierChain != null) {
            text = getImportText(identifierChain) + "." + text;
        }
        return text;
    }
}

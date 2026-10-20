package io.github.intellij.dlanguage.psi.ext;

import consulo.annotation.access.RequiredReadAction;
import consulo.d.language.inject.DStringLiteralEscaper;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.LeafElement;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.LiteralTextEscaper;
import consulo.language.psi.PsiLanguageInjectionHost;
import io.github.intellij.dlanguage.psi.DLanguageGccAsmTemplate;
import io.github.intellij.dlanguage.psi.DlangTypes;

/**
 * Consulo: the instruction template of a GCC style asm statement ({@code asm { "movl %1, %0" : ... }}), a host for the
 * assembly injected into it.
 */
public abstract class DLanguageGccAsmTemplateImplMixin extends ASTWrapperPsiElement implements DLanguageGccAsmTemplate, PsiLanguageInjectionHost {
    public DLanguageGccAsmTemplateImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(String text) {
        ASTNode literal = getNode().getFirstChildNode();
        if (literal instanceof LeafElement leaf) {
            leaf.replaceWithText(text);
        }
        return this;
    }

    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        ASTNode literal = getNode().getFirstChildNode();
        if (literal != null && literal.getElementType() == DlangTypes.DOUBLE_QUOTED_STRING) {
            return new DStringLiteralEscaper<>(this);
        }
        return LiteralTextEscaper.createSimple(this);
    }

    /**
     * The text between the quotes: {@code "..."}, {@code r"..."} or {@code `...`}, with an optional {@code c}, {@code w}
     * or {@code d} suffix.
     */
    @RequiredReadAction
    public TextRange getValueRange() {
        String text = getText();
        int start = text.startsWith("r\"") ? 2 : 1;
        int end = text.length();
        if (end > start && "cwd".indexOf(text.charAt(end - 1)) >= 0) {
            end--;
        }
        if (end > start) {
            end--;
        }
        return new TextRange(Math.min(start, end), end);
    }
}

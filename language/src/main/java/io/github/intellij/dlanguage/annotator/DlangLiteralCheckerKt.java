package io.github.intellij.dlanguage.annotator;

import consulo.d.localize.DLocalize;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageLiteralExpression;
import io.github.intellij.dlanguage.utils.DPsiLiteralUtil;

import java.util.regex.Pattern;

/**
 * Top-level functions of upstream {@code DlangLiteralChecker.kt}.
 */
public final class DlangLiteralCheckerKt {
    private static final Pattern HEXADECIMAL_DIGIT = Pattern.compile("[0-9a-fA-F]");
    private static final Pattern BINARY_DIGIT = Pattern.compile("[01]");

    private DlangLiteralCheckerKt() {
    }

    public static void checkLiteral(DLanguageLiteralExpression expressionElement, AnnotationHolder holder) {
        PsiElement element = expressionElement.getINTEGER_LITERAL();
        if (element != null) {
            String text = element.getText();
            if (DPsiLiteralUtil.INSTANCE.isHexadecimal(text) && !HEXADECIMAL_DIGIT.matcher(text.substring(2)).find()) {
                holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.literalHexadecimalNoDigits()).range(element).create();
            }
            else if (DPsiLiteralUtil.INSTANCE.isBinary(text) && !BINARY_DIGIT.matcher(text.substring(2)).find()) {
                holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.literalBinaryNoDigits()).range(element).create();
            }
            else {
                Long value = DPsiLiteralUtil.INSTANCE.parseIntegerLiteral(text);
                if (value == null) {
                    holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.literalIntegerTooLarge()).range(element).create();
                }
                else if (DPsiLiteralUtil.INSTANCE.hasLongNotUnsignedSuffix(text)
                    && !(DPsiLiteralUtil.INSTANCE.isHexadecimal(text) || DPsiLiteralUtil.INSTANCE.isBinary(text))
                    && Long.compareUnsigned(value, DPsiLiteralUtil.getSIGNED_LONG_MAX()) > 0) {
                    holder.newAnnotation(HighlightSeverity.ERROR, DLocalize.literalIntegerNotUnsignedOverflow()).range(element).create();
                }
            }
            return;
        }
    }
}

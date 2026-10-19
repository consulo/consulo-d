package io.github.intellij.dlanguage.psi.ext;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageLiteralExpression;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.utils.DPsiLiteralUtil;
import org.jspecify.annotations.Nullable;

public abstract class DLanguageLiteralExpressionImplMixin extends ASTWrapperPsiElement implements DLanguageLiteralExpression {
    /**
     * {@code ULong.MAX_VALUE}: integer values are unsigned 64-bit values (Kotlin {@code ULong} upstream) kept in a {@code long}
     * and compared with {@link Long#compareUnsigned(long, long)}.
     */
    private static final long ULONG_MAX_VALUE = 0xFFFF_FFFF_FFFF_FFFFL;

    public DLanguageLiteralExpressionImplMixin(ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable DType getDType() {
        if (getKW_THIS() != null) {
            PsiElement scope = getContext();
            while (scope != null) {
                if (scope instanceof UserDefinedType userDefinedType) {
                    return userDefinedType.getDType();
                }
                scope = scope.getContext();
            }
            return null;
        }
        if (getKW_SUPER() != null) {
            PsiElement scope = getContext();
            while (scope != null) {
                if (scope instanceof DLanguageClassDeclaration classDeclaration) {
                    return getSuperType(classDeclaration);
                }
                scope = scope.getContext();
            }
            return null;
        }
        if (getKW_NULL() != null) {
            return DPrimitiveType.fromText("null");
        }
        if (getKW_TRUE() != null || getKW_FALSE() != null) {
            return DPrimitiveType.fromText("bool");
        }
        PsiElement integerLiteral = getINTEGER_LITERAL();
        if (integerLiteral != null) {
            String text = integerLiteral.getText();
            long intValue = getLongValue(text);
            if (text.endsWith("uL") || text.endsWith("UL") || text.endsWith("Lu") || text.endsWith("LU")) {
                return DPrimitiveType.fromText("ulong");
            }
            else if (text.endsWith("u") || text.endsWith("U")) {
                if (Long.compareUnsigned(intValue, 4_294_967_295L) <= 0) {
                    return DPrimitiveType.fromText("uint");
                }
                return DPrimitiveType.fromText("ulong");
            }
            else if (text.endsWith("L")) {
                if ((DPsiLiteralUtil.INSTANCE.isHexadecimal(text) || DPsiLiteralUtil.INSTANCE.isBinary(text))
                    && Long.compareUnsigned(intValue, DPsiLiteralUtil.getSIGNED_LONG_MAX()) > 0) {
                    return DPrimitiveType.fromText("ulong");
                }
                // if a regular long (not binary not hex) is declared a higher limit than 9_223_372_036_854_775_807L,
                // then it’s an overflow, an error should be reported
                return DPrimitiveType.fromText("long");
            }
            else {
                // no extension
                if (Long.compareUnsigned(intValue, 2_147_483_647L) <= 0) {
                    return DPrimitiveType.fromText("int");
                }
                // binary and hexadecimal value can be uint even without a suffix
                if ((DPsiLiteralUtil.INSTANCE.isHexadecimal(text) || DPsiLiteralUtil.INSTANCE.isBinary(text))
                    && Long.compareUnsigned(intValue, 4_294_967_295L) <= 0) {
                    return DPrimitiveType.fromText("uint");
                }
                if (Long.compareUnsigned(intValue, DPsiLiteralUtil.getSIGNED_LONG_MAX()) <= 0) {
                    return DPrimitiveType.fromText("long");
                }
                return DPrimitiveType.fromText("ulong");
            }
        }
        PsiElement floatLiteral = getFLOAT_LITERAL();
        if (floatLiteral != null) {
            String text = floatLiteral.getText();
            char last = text.charAt(text.length() - 1);
            if (last == 'i') {
                char preLast = text.charAt(text.length() - 2);
                return switch (preLast) {
                    case 'f', 'F' -> DPrimitiveType.fromText("ifloat");
                    case 'L' -> DPrimitiveType.fromText("ireal");
                    default -> DPrimitiveType.fromText("idouble");
                };
            }
            return switch (last) {
                case 'f', 'F' -> DPrimitiveType.fromText("float");
                case 'L' -> DPrimitiveType.fromText("real");
                default -> DPrimitiveType.fromText("double");
            };
        }
        if (getCHARACTER_LITERAL() != null) {
            // TODO dchar and wchar (need to check the char value to determine his real type)
            return DPrimitiveType.fromText("char");
        }
        if (!getALTERNATE_WYSIWYG_STRINGs().isEmpty() || !getDOUBLE_QUOTED_STRINGs().isEmpty()
            || !getWYSIWYG_STRINGs().isEmpty() || !getDELIMITED_STRINGs().isEmpty() || !getTOKEN_STRINGs().isEmpty()
            || !getHEX_STRINGs().isEmpty()) {
            return new DArrayType(DPrimitiveType.fromText("char"), null); // TODO immutable (and handle wstring and dstring)
        }
        return null;
    }

    /**
     * @return the unsigned value of the literal (Kotlin {@code ULong} upstream)
     */
    private long getLongValue(String text) {
        String finalText = text;
        // remove the suffix, can loop at most twice
        while (finalText.endsWith("U") || finalText.endsWith("u") || finalText.endsWith("L")) {
            finalText = finalText.substring(0, finalText.length() - 1);
        }
        finalText = finalText.replace("_", "");

        if (DPsiLiteralUtil.INSTANCE.isBinary(finalText)) {
            finalText = finalText.substring(2);
            try {
                return Long.parseUnsignedLong(finalText, 2);
            }
            catch (NumberFormatException e) {
                // Number too high probably
                return ULONG_MAX_VALUE;
            }
        }
        if (DPsiLiteralUtil.INSTANCE.isHexadecimal(finalText)) {
            finalText = finalText.substring(2);
            try {
                return Long.parseUnsignedLong(finalText, 16);
            }
            catch (NumberFormatException e) {
                // Number too high probably
                return ULONG_MAX_VALUE;
            }
        }
        try {
            return Long.parseUnsignedLong(finalText);
        }
        catch (NumberFormatException e) {
            // Number too high probably
            return ULONG_MAX_VALUE;
        }
    }

    private @Nullable DType getSuperType(DLanguageClassDeclaration aClass) {
        DLanguageClassDeclaration parentClass = aClass.getParentClass();
        return parentClass != null ? parentClass.getDType() : null;
    }
}

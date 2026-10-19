package io.github.intellij.dlanguage.utils;

import org.jspecify.annotations.Nullable;

public final class DPsiLiteralUtil {
    public static final DPsiLiteralUtil INSTANCE = new DPsiLiteralUtil();

    private static final long SIGNED_LONG_MAX = 9_223_372_036_854_775_807L;

    private DPsiLiteralUtil() {
    }

    public static long getSIGNED_LONG_MAX() {
        return SIGNED_LONG_MAX;
    }

    public boolean isHexadecimal(String text) {
        return text.startsWith("0x") || text.startsWith("0X");
    }

    public boolean isBinary(String text) {
        return text.startsWith("0b") || text.startsWith("0B");
    }

    public boolean isDeclaredUnsignedLong(String text) {
        return text.endsWith("UL") || text.endsWith("uL") || text.endsWith("LU") || text.endsWith("Lu");
    }

    public boolean hasLongNotUnsignedSuffix(String text) {
        return !isDeclaredUnsignedLong(text) && (text.endsWith("L") || text.endsWith("l"));
    }

    /**
     * Upstream returns Kotlin's {@code ULong?}. Java has no unsigned long, so the value is returned as a {@code long}
     * holding the same 64 bits: compare it with {@link Long#compareUnsigned} and print it with
     * {@link Long#toUnsignedString}.
     */
    public @Nullable Long parseIntegerLiteral(String text) {
        String finalText = text;
        // remove the suffix, can loop at most twice
        while (finalText.endsWith("U") || finalText.endsWith("u") || finalText.endsWith("L")) {
            finalText = finalText.substring(0, finalText.length() - 1);
        }
        finalText = finalText.replace("_", "");

        if (isBinary(finalText)) {
            finalText = finalText.substring(2);
            try {
                return Long.parseUnsignedLong(finalText, 2);
            }
            catch (NumberFormatException e) {
                // Number too high probably
                return null;
            }
        }
        if (isHexadecimal(finalText)) {
            finalText = finalText.substring(2);
            try {
                return Long.parseUnsignedLong(finalText, 16);
            }
            catch (NumberFormatException e) {
                // Number too high probably
                return null;
            }
        }
        try {
            return Long.parseUnsignedLong(finalText);
        }
        catch (NumberFormatException e) {
            // Number too high probably
            return null;
        }
    }
}

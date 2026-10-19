package io.github.intellij.dlanguage.utils;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Top-level declarations of upstream's {@code DStringUtil.kt}.
 */
public final class DStringUtilKt {
    private static final List<Character> predefinedDelimiters = List.of('(', ')', '[', ']', '{', '}', '<', '>', '/', '\\');

    private DStringUtilKt() {
    }

    /**
     * Take the opening delimiter of a String and return the corresponding closing one
     */
    public static String getCorrespondingClosingDelimiter(String openingDelimiter) {
        return switch (openingDelimiter) {
            case "(" -> ")";
            case "[" -> "]";
            case "{" -> "}";
            case "<" -> ">";
            default -> openingDelimiter;
        };
    }

    public static boolean isPredefinedDelimiter(String literal) {
        return predefinedDelimiters.indexOf(literal.charAt(0)) != -1;
    }

    /**
     * Take a Delimited String content without the surrounding delimiters (`q"` and `"`) and
     * return the opening delimiter, or null if the delimiter is invalid.
     */
    public static @Nullable String getOpeningDelimiter(String literal) {
        if (isPredefinedDelimiter(literal)) {
            return String.valueOf(literal.charAt(0));
        }

        // Invalid delimiter
        if (Character.isWhitespace(literal.charAt(0))) {
            return null;
        }

        // otherwise it’s a id\n
        String[] lines = literal.split("\r\n|\n|\r", -1);
        if (lines.length == 1) {
            return null; // invalid string delimiter
        }
        return lines[0];
    }
}

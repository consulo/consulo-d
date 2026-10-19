package io.github.intellij.dlanguage.codeinsight;

/**
 * Top-level declarations of upstream's {@code ParameterInfo.kt}.
 */
final class ParameterInfoKt {
    private ParameterInfoKt() {
    }

    static String removeParentheses(String parameterText) {
        String text = parameterText;
        if (text.charAt(0) == '(') {
            text = text.substring(1);
        }
        if (text.charAt(text.length() - 1) == ')') {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}

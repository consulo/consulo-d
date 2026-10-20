package consulo.d.language.inject;

import consulo.document.util.TextRange;
import consulo.language.psi.LiteralTextEscaper;
import consulo.language.psi.PsiLanguageInjectionHost;

/**
 * The value of a D double quoted string literal ({@code "..."}): its escape sequences decoded, so injected code sees the
 * characters the compiler sees.
 */
public class DStringLiteralEscaper<T extends PsiLanguageInjectionHost> extends LiteralTextEscaper<T> {
    private int[] myOutSourceOffsets = new int[0];

    public DStringLiteralEscaper(T host) {
        super(host);
    }

    @Override
    public boolean decode(TextRange rangeInsideHost, StringBuilder outChars) {
        String text = rangeInsideHost.substring(myHost.getText());
        int[] sourceOffsets = new int[text.length() + 1];
        boolean result = parseStringCharacters(text, outChars, sourceOffsets);
        myOutSourceOffsets = sourceOffsets;
        return result;
    }

    @Override
    public int getOffsetInHost(int offsetInDecoded, TextRange rangeInsideHost) {
        int[] offsets = myOutSourceOffsets;
        if (offsetInDecoded < 0 || offsetInDecoded >= offsets.length) {
            return -1;
        }
        int result = offsets[offsetInDecoded];
        if (result == -1) {
            return -1;
        }
        return Math.min(result, rangeInsideHost.getLength()) + rangeInsideHost.getStartOffset();
    }

    @Override
    public boolean isOneLine() {
        return false;
    }

    /**
     * Appends the decoded characters of {@code chars} to {@code outChars}; {@code sourceOffsets[i]} is the offset in
     * {@code chars} of the {@code i}-th appended character, and the last entry the length of {@code chars}.
     */
    static boolean parseStringCharacters(String chars, StringBuilder outChars, int[] sourceOffsets) {
        int outOffset = outChars.length();
        int index = 0;
        while (index < chars.length()) {
            char c = chars.charAt(index);
            sourceOffsets[outChars.length() - outOffset] = index;
            if (c != '\\') {
                outChars.append(c);
                index++;
                continue;
            }
            if (index + 1 >= chars.length()) {
                return false;
            }
            char next = chars.charAt(index + 1);
            int before = outChars.length();
            int end = index + 2;
            switch (next) {
                case '\'', '"', '?', '\\' -> outChars.append(next);
                case 'a' -> outChars.append((char) 7);
                case 'b' -> outChars.append('\b');
                case 'f' -> outChars.append('\f');
                case 'n' -> outChars.append('\n');
                case 'r' -> outChars.append('\r');
                case 't' -> outChars.append('\t');
                case 'v' -> outChars.append((char) 11);
                case 'x' -> {
                    end = appendHex(chars, index + 2, 2, outChars);
                    if (end < 0) {
                        return false;
                    }
                }
                case 'u' -> {
                    end = appendHex(chars, index + 2, 4, outChars);
                    if (end < 0) {
                        return false;
                    }
                }
                case 'U' -> {
                    end = appendHex(chars, index + 2, 8, outChars);
                    if (end < 0) {
                        return false;
                    }
                }
                case '&' -> {
                    // named character entities are kept as written
                    int semicolon = chars.indexOf(';', index);
                    end = semicolon < 0 ? chars.length() : semicolon + 1;
                    outChars.append(chars, index, end);
                }
                default -> {
                    if (next < '0' || next > '7') {
                        return false;
                    }
                    int value = 0;
                    end = index + 1;
                    while (end < chars.length() && end < index + 4 && chars.charAt(end) >= '0' && chars.charAt(end) <= '7') {
                        value = value * 8 + (chars.charAt(end) - '0');
                        end++;
                    }
                    outChars.append((char) value);
                }
            }
            for (int i = before; i < outChars.length(); i++) {
                sourceOffsets[i - outOffset] = index;
            }
            index = end;
        }
        sourceOffsets[outChars.length() - outOffset] = chars.length();
        return true;
    }

    private static int appendHex(String chars, int start, int digits, StringBuilder outChars) {
        if (start + digits > chars.length()) {
            return -1;
        }
        int codePoint;
        try {
            codePoint = Integer.parseInt(chars.substring(start, start + digits), 16);
        }
        catch (NumberFormatException e) {
            return -1;
        }
        if (!Character.isValidCodePoint(codePoint)) {
            return -1;
        }
        outChars.appendCodePoint(codePoint);
        return start + digits;
    }
}

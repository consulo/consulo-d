package io.github.intellij.dlanguage.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import consulo.util.lang.CharArrayUtil;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;

import static consulo.language.ast.StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN;
import static consulo.language.ast.StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN;
import static consulo.language.ast.StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN;
import static io.github.intellij.dlanguage.psi.DlangTypes.VALID_NAMED_CHARACTER_ENTITY;

/**
 * Lex a token to generate check if all his content is valid, this is used for highlighting to show errors in strings.
 */
public class DLiteralEscapesLexer extends LexerBase {
    private final IElementType defaultToken;
    private int tokenStart = 0;
    private int tokenEnd = 0;
    private CharSequence bufferSequence = "";
    private int bufferEnd = 0;
    private int state = 0;

    public DLiteralEscapesLexer(IElementType defaultToken) {
        this.defaultToken = defaultToken;
    }

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.bufferSequence = buffer;
        bufferEnd = endOffset;
        state = initialState;

        tokenEnd = startOffset;
        advance();
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        // We’re at the end of the string token
        if (tokenStart >= tokenEnd) {
            return null;
        }

        // skip non-escapes sequences
        if (bufferSequence.charAt(tokenStart) != '\\') {
            return defaultToken;
        }

        if (tokenStart + 1 >= tokenEnd) {
            return INVALID_CHARACTER_ESCAPE_TOKEN;
        }

        return switch (bufferSequence.charAt(tokenStart + 1)) {
            case 'u', 'U' -> isValidUnicodeEscape(tokenStart, tokenEnd) ? VALID_STRING_ESCAPE_TOKEN : INVALID_UNICODE_ESCAPE_TOKEN;
            case 'x' -> isValidByteEscape(tokenStart, tokenEnd) ? VALID_STRING_ESCAPE_TOKEN : INVALID_CHARACTER_ESCAPE_TOKEN;
            case '\'', '"', '?', '\\', '0', 'a', 'b', 'f', 'n', 'r', 't', 'v' -> VALID_STRING_ESCAPE_TOKEN;
            case '&' -> isValidNamedCharacterEntities(tokenStart, tokenEnd) ? VALID_NAMED_CHARACTER_ENTITY : INVALID_CHARACTER_ESCAPE_TOKEN;
            case '$' -> defaultToken == DlangTypes.IES_TEXT ? VALID_STRING_ESCAPE_TOKEN : INVALID_CHARACTER_ESCAPE_TOKEN;
            default -> {
                if (isValidOctalDigit(tokenStart, tokenEnd)) {
                    yield VALID_STRING_ESCAPE_TOKEN;
                }
                if (isValidNamedCharacterEntities(tokenStart, tokenEnd)) {
                    yield VALID_NAMED_CHARACTER_ENTITY;
                }
                yield INVALID_CHARACTER_ESCAPE_TOKEN;
            }
        };
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        tokenEnd = locateToken(tokenStart);
    }

    @Override
    public CharSequence getBufferSequence() {
        return bufferSequence;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    /**
     * Find next token location.
     * @return end offset of the next token. If offset is equals to start, then it’s the end of the file
     */
    private int locateToken(int start) {
        if (start >= bufferEnd) {
            return start;
        }

        // escape sequence
        if (bufferSequence.charAt(start) == '\\') {
            int i = start + 1;

            if (i >= bufferEnd) {
                return bufferEnd;
            }

            int spaceIdx = CharArrayUtil.indexOf(bufferSequence, " ", i, bufferEnd);
            int tabIdx = CharArrayUtil.indexOf(bufferSequence, "\t", i, bufferEnd);
            spaceIdx = spaceIdx == -1 ? bufferEnd : spaceIdx;
            tabIdx = tabIdx == -1 ? bufferEnd : tabIdx;
            int endIdx = Math.min(tabIdx, spaceIdx);
            switch (bufferSequence.charAt(i)) {
                case 'u', 'U' -> {
                    int startIdx = bufferSequence.charAt(i) == 'u' ? 4 : 8;
                    return Math.min(i + startIdx + 1, endIdx);
                }
                case 'x' -> {
                    return Math.min(i + 2 + 1, endIdx);
                }
                case '&' -> {
                    int semicolonIdx = CharArrayUtil.indexOf(bufferSequence, ";", i, bufferEnd);
                    semicolonIdx = semicolonIdx == -1 ? bufferEnd : semicolonIdx;
                    return Math.min(semicolonIdx + 1, endIdx);
                }
                default -> {
                }
            }
            // is octal?
            if (isOctalDigit(bufferSequence.charAt(i))) {
                if (i + 1 < bufferEnd && isOctalDigit(bufferSequence.charAt(i + 1))) {
                    if (i + 2 < bufferEnd && isOctalDigit(bufferSequence.charAt(i + 2))) {
                        return i + 2 + 1;
                    }
                    else {
                        return i + 1 + 1;
                    }
                }
                else {
                    return i + 1;
                }
            }
            return i + 1;
        }
        // else find next escape sequence if any
        int idx = CharArrayUtil.indexOf(bufferSequence, "\\", start + 1, bufferEnd);
        return idx != -1 ? idx : bufferEnd;
    }

    private boolean isValidUnicodeEscape(int start, int end) {
        int expectedDigitCount = bufferSequence.charAt(start + 1) == 'U' ? 8 : 4;
        return end - start == expectedDigitCount + 2 && all(start + 2, end, StringUtil::isHexDigit);
    }

    private boolean isValidByteEscape(int start, int end) {
        return end - start == 4 && all(start + 2, end, StringUtil::isHexDigit);
    }

    private boolean isValidOctalDigit(int start, int end) {
        return end - start <= 4 && all(start + 1, end, DLiteralEscapesLexer::isOctalDigit);
    }

    private boolean isValidNamedCharacterEntities(int start, int end) {
        return bufferSequence.charAt(end - 1) == ';';
    }

    private boolean all(int start, int end, CharPredicate predicate) {
        for (int i = start; i < end; i++) {
            if (!predicate.test(bufferSequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * IntelliJ's {@code StringUtil.isOctalDigit}, which Consulo's {@code StringUtil} does not have.
     */
    private static boolean isOctalDigit(char c) {
        return '0' <= c && c <= '7';
    }

    private interface CharPredicate {
        boolean test(char c);
    }
}

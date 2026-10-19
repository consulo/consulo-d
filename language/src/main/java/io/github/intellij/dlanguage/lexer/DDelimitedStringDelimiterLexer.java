package io.github.intellij.dlanguage.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static consulo.language.ast.StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN;
import static io.github.intellij.dlanguage.psi.DlangTypes.DELIMITED_STRING;
import static io.github.intellij.dlanguage.utils.DStringUtilKt.getCorrespondingClosingDelimiter;
import static io.github.intellij.dlanguage.utils.DStringUtilKt.getOpeningDelimiter;
import static io.github.intellij.dlanguage.utils.DStringUtilKt.isPredefinedDelimiter;

/**
 * Lex the string to highlight the Delimiter in the delimitedString
 */
public class DDelimitedStringDelimiterLexer extends LexerBase {
    private int tokenStart = 0;
    private int tokenEnd = 0;
    private CharSequence bufferSequence = "";
    private int bufferEnd = 0;
    private int state = 0;
    private @Nullable String openingDelimiter = null;

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

        if (state == 2 || state == 4) {
            return VALID_STRING_ESCAPE_TOKEN;
        }

        return DELIMITED_STRING;
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
        if (tokenStart >= bufferEnd) {
            state = 0;
            return;
        }
        switch (state) {
            case 0 -> { // beginning q"
                tokenEnd += 2;
                assert bufferSequence.subSequence(tokenStart, tokenEnd).toString().equals("q\"");
                state = 1;
            }
            case 1 -> // delimiter
                tokenEnd = locateStartingToken(tokenStart);
            case 2 -> // token end
                tokenEnd = locateEndingToken(tokenStart);
            case 3 -> { // closing delimiter
                tokenEnd = tokenStart + getCorrespondingClosingDelimiter(Objects.requireNonNull(openingDelimiter)).length();
                state = 4;
            }
            default -> { // end of string
                tokenEnd = bufferEnd;
                state = 0;
            }
        }
    }

    @Override
    public CharSequence getBufferSequence() {
        return bufferSequence;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    private int locateStartingToken(int start) {
        openingDelimiter = getOpeningDelimiter(bufferSequence.subSequence(start, bufferEnd - 1).toString());
        if (openingDelimiter == null) {
            state = 0;
            return bufferEnd;
        }
        state = 2;
        return start + openingDelimiter.length();
    }

    private int locateEndingToken(int start) {
        String opening = Objects.requireNonNull(openingDelimiter);
        String delimiter = getCorrespondingClosingDelimiter(opening);
        state = 3;
        String rest = bufferSequence.subSequence(start, bufferEnd).toString();

        // for this case always the last one, lexer handle it (there can be some nesting of symbols)
        if (!delimiter.equals(opening)) {
            return start + rest.lastIndexOf(delimiter);
        }

        if (isPredefinedDelimiter(delimiter)) {
            return start + firstPartLength(rest, delimiter);
        }

        // the delimiter is a user defined text
        return start + firstPartLength(rest, "\n" + delimiter) + 1; // +1 as we added the \n character
    }

    /**
     * Kotlin's {@code text.split(delimiter)[0].length}.
     */
    private static int firstPartLength(String text, String delimiter) {
        int index = text.indexOf(delimiter);
        return index == -1 ? text.length() : index;
    }
}

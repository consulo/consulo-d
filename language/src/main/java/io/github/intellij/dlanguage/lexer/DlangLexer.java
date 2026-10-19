package io.github.intellij.dlanguage.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DlangLexer extends LexerBase {
    private final _DlangLexer myFlexLexer = new _DlangLexer();
    private CharSequence myBuffer = "";
    private @Nullable IElementType myTokenType = null;

    private int myTokenStart = 0;
    private int myTokenEnd = 0;

    private int myBufferEnd = 0;
    private int myState = 0;

    private int myTokenStringIndexStart = -1;
    private int myTokenStringBraceDepth = 0;

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer = buffer;
        myTokenEnd = startOffset;
        myTokenStart = startOffset;
        myBufferEnd = endOffset;
        myTokenType = null;
        myState = initialState;
        myFlexLexer.reset(myBuffer, startOffset, endOffset, initialState);
    }

    @Override
    public int getState() {
        locateToken();
        return myState;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        locateToken();
        return myTokenType;
    }

    @Override
    public int getTokenStart() {
        locateToken();
        return myTokenStart;
    }

    @Override
    public int getTokenEnd() {
        locateToken();
        return myTokenEnd;
    }

    @Override
    public void advance() {
        locateToken();
        myTokenType = null;
    }

    @Override
    public CharSequence getBufferSequence() {
        return myBuffer;
    }

    @Override
    public int getBufferEnd() {
        return myBufferEnd;
    }

    private void locateToken() {
        if (myTokenType != null) {
            return;
        }

        if (myTokenEnd == myBufferEnd) {
            myTokenStart = myBufferEnd;
            return;
        }

        myTokenStart = myTokenEnd;

        if (myFlexLexer.yystate() != 0) {
            flexLocateToken();
        }
        else if (myBuffer.charAt(myTokenStart) == 'q' && (myTokenStart + 1) < myBufferEnd && myBuffer.charAt(myTokenStart + 1) == '{') {
            myTokenStringIndexStart = myTokenStart;
            myTokenStringBraceDepth = 1;
            myTokenEnd = myTokenStart + 2;
            locateTokenStringEnd();
        }
        else {
            flexLocateToken();
        }

        if (myTokenEnd > myBufferEnd) {
            myTokenStart = myBufferEnd;
        }
    }

    private void locateTokenStringEnd() {
        while (myTokenEnd < myBufferEnd) {
            myTokenStart = myTokenEnd;
            if (myFlexLexer.yystate() != 0) {
                flexLocateToken();
            }
            else if (myBuffer.charAt(myTokenStart) == '{') {
                myTokenEnd = myTokenStart + 1;
                myTokenStringBraceDepth++;
            }
            else if (myBuffer.charAt(myTokenStart) == '}') {
                myTokenEnd = myTokenStart + 1;
                if (--myTokenStringBraceDepth == 0) {
                    break;
                }
            }
            else {
                flexLocateToken();
            }
        }
        if (myTokenEnd < myBufferEnd) {
            if (myBuffer.charAt(myTokenEnd) == 'c' || myBuffer.charAt(myTokenEnd) == 'w' || myBuffer.charAt(myTokenEnd) == 'd') {
                myTokenEnd += 1;
            }
        }
        myTokenStart = myTokenStringIndexStart;
        myTokenType = DlangTypes.TOKEN_STRING;
    }

    private void flexLocateToken() {
        myFlexLexer.goTo(myTokenStart);
        myState = myFlexLexer.yystate();
        try {
            myTokenType = myFlexLexer.advance();
        }
        catch (IOException e) {
            // Kotlin has no checked exceptions: upstream let the exception propagate as is
            throw new UncheckedIOException(e);
        }
        myTokenEnd = myFlexLexer.getTokenEnd();
    }
}

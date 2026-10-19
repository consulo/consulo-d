package io.github.intellij.dlanguage.features.documentation;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import consulo.language.lexer.MergingLexerAdapter;
import consulo.util.lang.CharArrayUtil;

import java.io.IOException;

import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_LEADING_ASTERISKS;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_WHITESPACE;

public class DDocLexer extends MergingLexerAdapter {
    public DDocLexer() {
        super(new AsteriskStripperLexer(new _DDocLexer(null)), DDocLexerKt.getDdocTokenMerge());
    }

    private static class AsteriskStripperLexer extends LexerBase {
        private final _DDocLexer myFlex;

        private CharSequence myBuffer;
        private int myBufferIndex = 0;
        private int myBufferEndOffset = 0;
        private int myTokenEndOffset = 0;
        private int myState = 0;
        private IElementType myTokenType = null;
        private Character myLeadingAsteriskChar = null;

        private boolean myIsAfterLineBreak = false;
        private boolean myIsInLeadingSpace = false;

        AsteriskStripperLexer(_DDocLexer flex) {
            myFlex = flex;
        }

        @Override
        public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
            myBuffer = buffer;
            myBufferIndex = startOffset;
            myBufferEndOffset = endOffset;
            myTokenType = null;
            myTokenEndOffset = startOffset;
            myLeadingAsteriskChar = buffer.charAt(startOffset + 1);
            myFlex.reset(myBuffer, startOffset, endOffset, initialState);
        }

        @Override
        public int getState() {
            return myState;
        }

        @Override
        public CharSequence getBufferSequence() {
            return myBuffer;
        }

        @Override
        public int getBufferEnd() {
            return myBufferEndOffset;
        }

        @Override
        public IElementType getTokenType() {
            locateToken();
            return myTokenType;
        }

        @Override
        public int getTokenStart() {
            locateToken();
            return myBufferIndex;
        }

        @Override
        public int getTokenEnd() {
            locateToken();
            return myTokenEndOffset;
        }

        @Override
        public void advance() {
            locateToken();
            myTokenType = null;
        }

        private void locateToken() {
            if (myTokenType != null) {
                return;
            }
            doLocateToken();

            if (myTokenType == DDOC_WHITESPACE) {
                myIsAfterLineBreak = CharArrayUtil.containLineBreaks(myBuffer, getTokenStart(), getTokenEnd());
            }
        }

        private void doLocateToken() {
            if (myTokenEndOffset == myBufferEndOffset) {
                myTokenType = null;
                myBufferIndex = myBufferEndOffset;
                return;
            }

            myBufferIndex = myTokenEndOffset;

            if (myIsAfterLineBreak) {
                myIsAfterLineBreak = false;
                myIsInLeadingSpace = true;

                // skip leading star (or +)
                if (myTokenEndOffset < myBufferEndOffset && myLeadingAsteriskChar != null
                    && myBuffer.charAt(myTokenEndOffset) == myLeadingAsteriskChar
                    && (myTokenEndOffset + 1 >= myBufferEndOffset || myBuffer.charAt(myTokenEndOffset + 1) != '/')) {
                    myTokenEndOffset++;
                }

                if (myBufferIndex < myTokenEndOffset) {
                    myTokenType = DDOC_COMMENT_LEADING_ASTERISKS;
                    return;
                }
            }

            if (myIsInLeadingSpace) {
                myIsInLeadingSpace = false;
                if (myFlex.yystate() == _DDocLexer.EMBEDDED_CODE) {
                    myFlex.yybegin(_DDocLexer.EMBEDDED_CODE_START);
                }
                // Don’t change state if we are in macro
                else if (myFlex.yystate() != _DDocLexer.DOC_MACRO && myFlex.yystate() != _DDocLexer.DOC_MACRO_START) {
                    myFlex.yybegin(_DDocLexer.COMMENT_DATA_START);
                }
            }

            flexLocateToken();
        }

        private void flexLocateToken() {
            try {
                myState = myFlex.yystate();
                myFlex.goTo(myBufferIndex);
                myTokenType = myFlex.advance();
                myTokenEndOffset = myFlex.getTokenEnd();
            }
            catch (IOException e) {
                // Can’t be
            }
        }
    }
}

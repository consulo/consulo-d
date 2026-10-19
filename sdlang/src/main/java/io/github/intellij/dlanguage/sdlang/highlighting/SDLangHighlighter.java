package io.github.intellij.dlanguage.sdlang.highlighting;

import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import io.github.intellij.dlanguage.sdlang.colors.SDLangColor;
import io.github.intellij.dlanguage.sdlang.lexer.SDLangHighlightingLexer;
import io.github.intellij.dlanguage.sdlang.psi.ElementTypesKt;
import io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes;
import org.jspecify.annotations.Nullable;

public class SDLangHighlighter extends SyntaxHighlighterBase {

    @Override
    public Lexer getHighlightingLexer() {
        return new SDLangHighlightingLexer();
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        SDLangColor color = tokenSetMap(tokenType);
        return pack(color == null ? null : color.getTextAttributesKey());
    }

    private @Nullable SDLangColor tokenSetMap(@Nullable IElementType tokenType) {
        if (tokenType == TokenType.BAD_CHARACTER) {
            return SDLangColor.ILLEGAL;
        }
        if (tokenType == SDLangElementTypes.LINE_COMMENT) {
            return SDLangColor.LINE_COMMENT;
        }
        if (tokenType == SDLangElementTypes.BLOCK_COMMENT) {
            return SDLangColor.BLOCK_COMMENT;
        }
        if (ElementTypesKt.SDLANG_STRINGS.contains(tokenType)) {
            return SDLangColor.STRING;
        }
        if (tokenType == SDLangElementTypes.TRUE || tokenType == SDLangElementTypes.FALSE || tokenType == SDLangElementTypes.NULL) {
            return SDLangColor.KEYWORD;
        }
        if (tokenType == SDLangElementTypes.INTEGER || tokenType == SDLangElementTypes.LONG_INT || tokenType == SDLangElementTypes.FLOAT
            || tokenType == SDLangElementTypes.DOUBLE || tokenType == SDLangElementTypes.DECIMAL) {
            return SDLangColor.NUMBER;
        }
        if (tokenType == SDLangElementTypes.DATE || tokenType == SDLangElementTypes.TIME || tokenType == SDLangElementTypes.DATE_TIME) {
            return SDLangColor.DATETIME;
        }
        if (tokenType == SDLangElementTypes.SEMICOLON) {
            return SDLangColor.SEMICOLON;
        }
        if (tokenType == SDLangElementTypes.L_CURLY || tokenType == SDLangElementTypes.R_CURLY) {
            return SDLangColor.BRACES;
        }
        return null;
    }
}

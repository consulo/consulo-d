package io.github.intellij.dlanguage.highlighting;

import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.ast.StringEscapesTokenTypes;
import consulo.language.ast.TokenType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import io.github.intellij.dlanguage.colors.DColor;
import io.github.intellij.dlanguage.lexer.DHighlightingLexer;
import io.github.intellij.dlanguage.psi.DlangTypes;
import org.jspecify.annotations.Nullable;

/**
 * Highlighter for D source. Uses DHighlightingLexer to create tokens of
 * DElementType, then returns the highlighting TextAttributesKey associated
 * with the DElementType token.
 */
public class DHighlighter extends SyntaxHighlighterBase {

    @Override
    public Lexer getHighlightingLexer() {
        return new DHighlightingLexer();
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        DColor color = tokenSetMap(tokenType);
        return pack(color == null ? null : color.getTextAttributesKey());
    }

    private @Nullable DColor tokenSetMap(IElementType tokenType) {
        if (tokenType == TokenType.BAD_CHARACTER) {
            return DColor.ILLEGAL;
        }
        if (tokenType == DlangTypes.SHEBANG) {
            return DColor.SHEBANG;
        }
        if (tokenType == DlangTypes.KW_PRAGMA) {
            return DColor.PRAGMA;
        }
        if (DHighlightingTokenSets.LINE_COMMENT.contains(tokenType)) {
            return DColor.LINE_COMMENT;
        }
        if (DHighlightingTokenSets.LINE_DOCUMENTATION.contains(tokenType)) {
            return DColor.LINE_DOCUMENTATION;
        }
        if (DHighlightingTokenSets.BLOCK_COMMENT.contains(tokenType)) {
            return DColor.BLOCK_COMMENT;
        }
        if (DHighlightingTokenSets.BLOCK_DOCUMENTATION.contains(tokenType)) {
            return DColor.BLOCK_DOCUMENTATION;
        }
        if (DHighlightingTokenSets.STRING_LITERAL.contains(tokenType)) {
            return DColor.STRING_LITERAL;
        }
        if (DHighlightingTokenSets.CHAR.contains(tokenType)) {
            return DColor.CHAR;
        }
        if (DHighlightingTokenSets.NUMBER_LITERALS.contains(tokenType)) {
            return DColor.NUMBER;
        }
        if (DHighlightingTokenSets.OPERATOR.contains(tokenType)) {
            return DColor.OPERATOR;
        }
        if (DHighlightingTokenSets.PARENTHESES.contains(tokenType)) {
            return DColor.PARENTHESES;
        }
        if (DHighlightingTokenSets.BRACES.contains(tokenType)) {
            return DColor.BRACES;
        }
        if (DHighlightingTokenSets.BRACKETS.contains(tokenType)) {
            return DColor.BRACKETS;
        }
        if (DHighlightingTokenSets.SEMICOLON.contains(tokenType)) {
            return DColor.SEMICOLON;
        }
        if (DHighlightingTokenSets.COMMA.contains(tokenType)) {
            return DColor.COMMA;
        }
        if (DHighlightingTokenSets.DOT.contains(tokenType)) {
            return DColor.DOT;
        }
        if (DHighlightingTokenSets.KEYWORD.contains(tokenType)) {
            return DColor.KEYWORD;
        }
        if (DHighlightingTokenSets.KW_SPECIAL_KEYWORDS.contains(tokenType)) {
            return DColor.SPECIAL_KEYWORD;
        }
        if (DHighlightingTokenSets.KW_SPECIAL_TOKENS.contains(tokenType)) {
            return DColor.SPECIAL_TOKEN;
        }
        if (tokenType == DlangTypes.IES_START || tokenType == DlangTypes.IES_END || tokenType == DlangTypes.IES_TEXT) {
            return DColor.STRING_LITERAL;
        }
        if (tokenType == DlangTypes.IES_FRAGMENT_START || tokenType == DlangTypes.IES_FRAGMENT_END) {
            return DColor.IES_FRAGMENT_DELIMITER;
        }
        if (tokenType == StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN) {
            return DColor.VALID_STRING_ESCAPE;
        }
        if (tokenType == StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN) {
            return DColor.INVALID_STRING_ESCAPE;
        }
        if (tokenType == StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN) {
            return DColor.INVALID_STRING_ESCAPE;
        }
        if (tokenType == DlangTypes.VALID_NAMED_CHARACTER_ENTITY) {
            return DColor.STRING_NAMED_CHARACTER_ENTITY;
        }
        return null;
    }
}

package io.github.intellij.dlanguage.sdlang.colors;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.d.localize.DLocalize;
import consulo.localize.LocalizeValue;

public enum SDLangColor {

    LINE_COMMENT(DLocalize.sdlangColorLineComment(), DefaultLanguageHighlighterColors.LINE_COMMENT),
    BLOCK_COMMENT(DLocalize.sdlangColorBlockComment(), DefaultLanguageHighlighterColors.BLOCK_COMMENT),

    KEYWORD(DLocalize.sdlangColorKeyword(), DefaultLanguageHighlighterColors.KEYWORD),
    NUMBER(DLocalize.sdlangColorNumber(), DefaultLanguageHighlighterColors.NUMBER),

    DATETIME(DLocalize.sdlangColorDatetime(), DefaultLanguageHighlighterColors.NUMBER),

    STRING(DLocalize.sdlangColorString(), DefaultLanguageHighlighterColors.STRING),
    ILLEGAL(DLocalize.sdlangColorIllegal(), DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE),

    BRACES(DLocalize.sdlangColorBraces(), DefaultLanguageHighlighterColors.BRACES),
    BRACKETS(DLocalize.sdlangColorBrackets(), DefaultLanguageHighlighterColors.BRACKETS),
    SEMICOLON(DLocalize.sdlangColorSemicolon(), DefaultLanguageHighlighterColors.SEMICOLON),
    COLON(DLocalize.sdlangColorColon(), DefaultLanguageHighlighterColors.SEMICOLON),

    TAG_IDENTIFIER(DLocalize.sdlangColorTagIdentifier(), DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    ;

    private final TextAttributesKey myDefault;
    private final TextAttributesKey myTextAttributesKey;
    private final AttributesDescriptor myAttributesDescriptor;

    SDLangColor(LocalizeValue humanName, TextAttributesKey defaultKey) {
        myDefault = defaultKey;
        myTextAttributesKey = TextAttributesKey.of("io.github.intellij.dlanguage.sdlang." + name(), defaultKey);
        myAttributesDescriptor = new AttributesDescriptor(humanName, myTextAttributesKey);
    }

    public TextAttributesKey getDefault() {
        return myDefault;
    }

    public TextAttributesKey getTextAttributesKey() {
        return myTextAttributesKey;
    }

    public AttributesDescriptor getAttributesDescriptor() {
        return myAttributesDescriptor;
    }
}

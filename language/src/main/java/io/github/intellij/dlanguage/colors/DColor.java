package io.github.intellij.dlanguage.colors;

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.d.localize.DLocalize;
import consulo.localize.LocalizeValue;

/**
 * The enum entries here are mapped in DHighlighter based on the DlangTypes supported by the highlighting lexer.
 * @see io.github.intellij.dlanguage.highlighting.DHighlightingTokenSets
 * @see io.github.intellij.dlanguage.psi.DlangTypes
 */
public enum DColor {

    SHEBANG(DLocalize.dColorShebang(), DefaultLanguageHighlighterColors.METADATA),

    LINE_COMMENT(DLocalize.dColorLineComment(), DefaultLanguageHighlighterColors.LINE_COMMENT),
    BLOCK_COMMENT(DLocalize.dColorBlockComment(), DefaultLanguageHighlighterColors.BLOCK_COMMENT),

    LINE_DOCUMENTATION(DLocalize.dColorLineDocumentation(), DefaultLanguageHighlighterColors.DOC_COMMENT),
    BLOCK_DOCUMENTATION(DLocalize.dColorBlockDocumentation(), DefaultLanguageHighlighterColors.DOC_COMMENT),

    AT_ATTRIBUTE(DLocalize.dColorAtAttribute(), DefaultLanguageHighlighterColors.METADATA),

    KEYWORD(DLocalize.dColorKeyword(), DefaultLanguageHighlighterColors.KEYWORD),
    PRAGMA(DLocalize.dColorPragma(), DefaultLanguageHighlighterColors.KEYWORD), // by default is same colour as other keywords but separated so can be configured differently
    SPECIAL_KEYWORD(DLocalize.dColorSpecialKeyword(), DefaultLanguageHighlighterColors.KEYWORD),
    SPECIAL_TOKEN(DLocalize.dColorSpecialToken(), DefaultLanguageHighlighterColors.KEYWORD),

    NUMBER(DLocalize.dColorNumber(), DefaultLanguageHighlighterColors.NUMBER),

    CHAR(DLocalize.dColorChar(), DefaultLanguageHighlighterColors.STRING),
    STRING_LITERAL(DLocalize.dColorStringLiteral(), DefaultLanguageHighlighterColors.STRING),
    IES_FRAGMENT_DELIMITER(DLocalize.dColorIesFragmentDelimiter(), DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    VALID_STRING_ESCAPE(DLocalize.dColorValidStringEscape(), DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    INVALID_STRING_ESCAPE(DLocalize.dColorInvalidStringEscape(), DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE),
    STRING_NAMED_CHARACTER_ENTITY(DLocalize.dColorStringNamedCharacterEntity(), DefaultLanguageHighlighterColors.MARKUP_ENTITY),
    ILLEGAL(DLocalize.dColorIllegal(), DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE),

    PARENTHESES(DLocalize.dColorParentheses(), DefaultLanguageHighlighterColors.PARENTHESES),
    BRACES(DLocalize.dColorBraces(), DefaultLanguageHighlighterColors.BRACES),
    BRACKETS(DLocalize.dColorBrackets(), DefaultLanguageHighlighterColors.BRACKETS),
    OPERATOR(DLocalize.dColorOperator(), DefaultLanguageHighlighterColors.OPERATION_SIGN),
    DOT(DLocalize.dColorDot(), DefaultLanguageHighlighterColors.DOT),
    SEMICOLON(DLocalize.dColorSemicolon(), DefaultLanguageHighlighterColors.SEMICOLON),
    COMMA(DLocalize.dColorComma(), DefaultLanguageHighlighterColors.COMMA),

    CLASS_DEFINITION(DLocalize.dColorClassDefinition(), DefaultLanguageHighlighterColors.CLASS_NAME),
    INTERFACE_DEFINITION(DLocalize.dColorInterfaceDefinition(), CLASS_DEFINITION.getTextAttributesKey()),
    STRUCT_DEFINITION(DLocalize.dColorStructDefinition(), CLASS_DEFINITION.getTextAttributesKey()),
    UNION_DEFINITION(DLocalize.dColorUnionDefinition(), CLASS_DEFINITION.getTextAttributesKey()),
    ENUM_DEFINITION(DLocalize.dColorEnumDefinition(), CLASS_DEFINITION.getTextAttributesKey()),

    MODULE_DEFINITION(DLocalize.dColorModuleDefinition(), DefaultLanguageHighlighterColors.GLOBAL_VARIABLE),
    FUNCTION_DEFINITION(DLocalize.dColorFunctionDefinition(), DefaultLanguageHighlighterColors.FUNCTION_DECLARATION),
    FUNCTION_CALL(DLocalize.dColorFunctionCall(), DefaultLanguageHighlighterColors.FUNCTION_CALL),
    //IDENTIFIER("Identifier", Default.IDENTIFIER),
    PARAMETER(DLocalize.dColorParameter(), DefaultLanguageHighlighterColors.PARAMETER),
    TYPE_PARAMETER(DLocalize.dColorTypeParameter(), DefaultLanguageHighlighterColors.PARAMETER),
    ;

    private final TextAttributesKey myDefault;
    private final TextAttributesKey myTextAttributesKey;
    private final AttributesDescriptor myAttributesDescriptor;

    DColor(LocalizeValue humanName, TextAttributesKey defaultKey) {
        myDefault = defaultKey;
        myTextAttributesKey = TextAttributesKey.of("io.github.intellij.dlanguage." + name(), defaultKey);
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

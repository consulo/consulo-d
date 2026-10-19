package io.github.intellij.dlanguage.features.documentation;

import io.github.intellij.dlanguage.documentation.psi.DlangDocElementType;

public final class DDocElementTypes {
    public static final DDocElementTypes INSTANCE = new DDocElementTypes();

    // Tokens from Lexer
    public static final DlangDocElementType DDOC_WHITESPACE = new DlangDocElementType("DDOC_WHITESPACE");
    public static final DlangDocElementType DDOC_COMMENT_DATA = new DlangDocElementType("DDOC_COMMENT_DATA");
    public static final DlangDocElementType DDOC_COLON = new DlangDocElementType("DDOC_COLON");
    public static final DlangDocElementType DDOC_QUOTE_CHAR = new DlangDocElementType("DDOC_QUOTE_CHAR");
    public static final DlangDocElementType DDOC_EXCLAMATION_MARK = new DlangDocElementType("DDOC_EXCLAMATION_MARK");
    public static final DlangDocElementType DDOC_LEFT_BRACKET = new DlangDocElementType("DDOC_LEFT_BRACKET");
    public static final DlangDocElementType DDOC_RIGHT_BRACKET = new DlangDocElementType("DDOC_RIGHT_BRACKET");
    public static final DlangDocElementType DDOC_LEFT_PARENTHESES = new DlangDocElementType("DDOC_LEFT_PARENTHESES");
    public static final DlangDocElementType DDOC_RIGHT_PARENTHESES = new DlangDocElementType("DDOC_RIGHT_PARENTHESES");
    public static final DlangDocElementType DDOC_BAD_CHARACTER = new DlangDocElementType("DDOC_BAD_CHARACTER");
    public static final DlangDocElementType DDOC_EMBEDDED_CODE_DELIMITER = new DlangDocElementType("DDOC_EMBEDDED_CODE_DELIMITER");
    public static final DlangDocElementType DDOC_EMBEDDED_CODE_CONTENT = new DlangDocElementType("DDOC_EMBEDDED_CODE_CONTENT");
    public static final DlangDocElementType DDOC_HEADING_CHARS = new DlangDocElementType("DDOC_HEADING_CHARS");
    public static final DlangDocElementType DDOC_ORDERED_LIST_POINT = new DlangDocElementType("DDOC_ORDERED_LIST_POINT");
    public static final DlangDocElementType DDOC_UNORDERED_LIST_POINT = new DlangDocElementType("DDOC_UNORDERED_LIST_POINT");
    public static final DlangDocElementType DDOC_ID = new DlangDocElementType("DDOC_ID");
    public static final DlangDocElementType DDOC_EQ = new DlangDocElementType("DDOC_EQ");

    public static final DlangDocElementType DDOC_MACRO_OPEN = new DlangDocElementType("DDOC_MACRO_OPEN");
    public static final DlangDocElementType DDOC_MACRO_END = new DlangDocElementType("DDOC_MACRO_END");
    public static final DlangDocElementType DDOC_MACRO_COMA = new DlangDocElementType("DDOC_MACRO_COMA");
    public static final DlangDocElementType DDOC_MACRO_BACKSLASH = new DlangDocElementType("DDOC_MACRO_BACKSLASH");
    public static final DlangDocElementType DDOC_MACRO_EQUALS = new DlangDocElementType("DDOC_MACRO_EQUALS");
    public static final DlangDocElementType DDOC_MACRO_IDENTIFIER = new DlangDocElementType("DDOC_MACRO_IDENTIFIER");
    public static final DlangDocElementType DDOC_MACRO_ARGUMENT = new DlangDocElementType("DDOC_MACRO_ARGUMENT");

    // Highlighting
    public static final DlangDocElementType DDOC_EMBEDDED_CODE = new DlangDocElementType("DDOC_EMBEDDED_CODE");
    public static final DlangDocElementType DDOC_INLINE_CODE = new DlangDocElementType("DDOC_INLINE_CODE");
    public static final DlangDocElementType DDOC_HEADING = new DlangDocElementType("DDOC_HEADING");
    public static final DlangDocElementType DDOC_QUOTE = new DlangDocElementType("DDOC_QUOTE");
    public static final DlangDocElementType DDOC_LIST_ITEM = new DlangDocElementType("DDOC_LIST_ITEM");
    public static final DlangDocElementType DDOC_LIST_ITEM_CONTENT = new DlangDocElementType("DDOC_LIST_ITEM_CONTENT");
    public static final DlangDocElementType DDOC_ORDERED_LIST = new DlangDocElementType("DDOC_ORDERED_LIST");
    public static final DlangDocElementType DDOC_UNORDERED_LIST = new DlangDocElementType("DDOC_UNORDERED_LIST");
    public static final DlangDocElementType DDOC_HORIZONTAL_RULE = new DlangDocElementType("DDOC_HORIZONTAL_RULE");
    public static final DlangDocElementType DDOC_SIMPLE_EMPHASIS = new DlangDocElementType("DDOC_SIMPLE_EMPHASIS");
    public static final DlangDocElementType DDOC_DOUBLE_EMPHASIS = new DlangDocElementType("DDOC_DOUBLE_EMPHASIS");
    public static final DlangDocElementType DDOC_TRIPLE_EMPHASIS = new DlangDocElementType("DDOC_TRIPLE_EMPHASIS");
    public static final DlangDocElementType DDOC_MACRO_CALL = new DlangDocElementType("DDOC_MACRO_CALL");
    public static final DlangDocElementType DDOC_LINK = new DlangDocElementType("DDOC_LINK");
    public static final DlangDocElementType DDOC_LINK_DECLARATION = new DlangDocElementType("DDOC_LINK_DECLARATION");
    public static final DlangDocElementType DDOC_IMAGE = new DlangDocElementType("DDOC_IMAGE");
    public static final DlangDocElementType DDOC_KEY_VALUE_ELEMENT = new DlangDocElementType("DDOC_KEY_VALUE_ELEMENT");

    // Intermediate Content for highlighting
    public static final DlangDocElementType DDOC_LINK_TEXT = new DlangDocElementType("DDOC_LINK_TEXT");
    public static final DlangDocElementType DDOC_LINK_NAME = new DlangDocElementType("DDOC_LINK_NAME");
    public static final DlangDocElementType DDOC_LINK_REFERENCE_TO = new DlangDocElementType("DDOC_LINK_REFERENCE_TO");
    public static final DlangDocElementType DDOC_LINK_INLINE_REFERENCE_TEXT = new DlangDocElementType("DDOC_LINK_INLINE_REFERENCE_TEXT");

    // Sections
    public static final DlangDocElementType DDOC_SECTION_TITLE = new DlangDocElementType("DDOC_SECTION_TITLE");
    public static final DlangDocElementType DDOC_SECTION_PARAGRAPH = new DlangDocElementType("DDOC_SECTION_PARAGRAPH");
    public static final DlangDocElementType DDOC_NAMED_SECTION = new DlangDocElementType("DDOC_NAMED_SECTION");

    public static final DlangDocElementType DDOC_SUMMARY_SECTION = new DlangDocElementType("DDOC_SUMMARY_SECTION"); // unnamed
    public static final DlangDocElementType DDOC_DESCRIPTION_SECTION = new DlangDocElementType("DDOC_DESCRIPTION_SECTION"); // unnamed
    public static final DlangDocElementType DDOC_AUTHORS_SECTION = new DlangDocElementType("DDOC_AUTHORS_SECTION");
    public static final DlangDocElementType DDOC_BUGS_SECTION = new DlangDocElementType("DDOC_BUGS_SECTION");
    public static final DlangDocElementType DDOC_DATE_SECTION = new DlangDocElementType("DDOC_DATE_SECTION");
    public static final DlangDocElementType DDOC_DEPRECATED_SECTION = new DlangDocElementType("DDOC_DEPRECATED_SECTION");
    public static final DlangDocElementType DDOC_EXAMPLES_SECTION = new DlangDocElementType("DDOC_EXAMPLES_SECTION");
    public static final DlangDocElementType DDOC_HISTORY_SECTION = new DlangDocElementType("DDOC_HISTORY_SECTION");
    public static final DlangDocElementType DDOC_LICENSE_SECTION = new DlangDocElementType("DDOC_LICENSE_SECTION");
    public static final DlangDocElementType DDOC_RETURNS_SECTION = new DlangDocElementType("DDOC_RETURNS_SECTION");
    public static final DlangDocElementType DDOC_SEE_ALSO_SECTION = new DlangDocElementType("DDOC_SEE_ALSO_SECTION");
    public static final DlangDocElementType DDOC_STANDARDS_SECTION = new DlangDocElementType("DDOC_STANDARDS_SECTION");
    public static final DlangDocElementType DDOC_THROWS_SECTION = new DlangDocElementType("DDOC_THROWS_SECTION");
    public static final DlangDocElementType DDOC_VERSION_SECTION = new DlangDocElementType("DDOC_VERSION_SECTION");

    // Specials sections
    public static final DlangDocElementType DDOC_COPYRIGHT_SECTION = new DlangDocElementType("DDOC_COPYRIGHT_SECTION"); // Module level only
    public static final DlangDocElementType DDOC_PARAMS_SECTION = new DlangDocElementType("DDOC_PARAMS_SECTION"); // Function level, special syntax
    public static final DlangDocElementType DDOC_MACROS_SECTION = new DlangDocElementType("DDOC_MACROS_SECTION"); // Same syntax as params


    // TODO Remain Highlighting
    /*
    TABLES
    IDENTIFIER_EMPHASIS // highlighting ref (see java)
    ~ PUNCTUATION_ESCAPES // mainly macro but need to support \\
    */

    private DDocElementTypes() {
    }
}

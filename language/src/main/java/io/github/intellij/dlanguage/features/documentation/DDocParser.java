package io.github.intellij.dlanguage.features.documentation;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersion;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_END;
import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_LEADING_ASTERISKS;
import static io.github.intellij.dlanguage.documentation.psi.DDocMetaElementTypes.DDOC_COMMENT_START;

public class DDocParser implements PsiParser {
    private static final TokenSet SKIP_TOKENS = TokenSet.create(DDOC_COMMENT_LEADING_ASTERISKS);

    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder, LanguageVersion languageVersion) {
        parseLight(root, builder);
        return builder.getTreeBuilt();
    }

    // Consulo has no LightPsiParser: kept as a plain method
    public void parseLight(IElementType root, PsiBuilder builder) {
        new DDocParserImpl(builder).parse(root);
    }

    private static class DDocParserImpl {
        private final PsiBuilder myBuilder;

        DDocParserImpl(PsiBuilder builder) {
            myBuilder = builder;
        }

        void parse(IElementType root) {
            myBuilder.enforceCommentTokens(SKIP_TOKENS);
            PsiBuilder.Marker marker = myBuilder.mark();
            if (myBuilder.getTokenType() == DDOC_COMMENT_START) {
                myBuilder.advanceLexer();
            }
            parseSections();
            // Swallow all ending tokens that may exist
            while (!myBuilder.eof()) {
                myBuilder.advanceLexer();
            }
            marker.done(root);
        }

        private boolean isBlankToken() {
            return isBlankToken(myBuilder.getTokenType());
        }

        private boolean isBlankToken(@Nullable IElementType tokenType) {
            return tokenType == DDocElementTypes.DDOC_WHITESPACE || tokenType == TokenType.WHITE_SPACE;
        }

        private boolean isEndOfComment() {
            return myBuilder.eof() || myBuilder.getTokenType() == DDOC_COMMENT_END;
        }

        private boolean hasNewLine() {
            String tokenText = myBuilder.getTokenText();
            return isBlankToken() && tokenText != null && tokenText.contains("\n");
        }

        private boolean has2NewLine() {
            if (!isBlankToken()) {
                return false;
            }
            String tokenText = myBuilder.getTokenText();
            int newLineCount = tokenText != null ? countNewLines(tokenText) : 0;
            int i = 1;
            IElementType next = myBuilder.rawLookup(i);
            // Note: could avoid this if-block if it was able to get text of next token
            if (next == DDOC_COMMENT_LEADING_ASTERISKS) {
                i++;
                next = myBuilder.rawLookup(i);
            }
            if (next == DDocElementTypes.DDOC_WHITESPACE) {
                CharSequence text = rawLookupText(i);
                newLineCount += countNewLines(text);
            }
            return newLineCount >= 2;
        }

        private static int countNewLines(CharSequence text) {
            int count = 0;
            for (int j = 0; j < text.length(); j++) {
                if (text.charAt(j) == '\n') {
                    count++;
                }
            }
            return count;
        }

        private void consumeBlankLines() {
            while (isBlankToken(getTokenType()) && !isEndOfComment()) {
                myBuilder.advanceLexer();
            }
        }

        private CharSequence rawLookupText(int steps) {
            int start = myBuilder.rawTokenTypeStart(steps);
            int end = myBuilder.rawTokenTypeStart(steps + 1);
            return start == -1 || end == -1 ? "" : myBuilder.getOriginalText().subSequence(start, end);
        }

        private @Nullable IElementType getTokenType() {
            IElementType tokenType = myBuilder.getTokenType();
            if (myBuilder.getTokenType() == DDocElementTypes.DDOC_WHITESPACE) {
                myBuilder.remapCurrentToken(TokenType.WHITE_SPACE);
            }
            return tokenType;
        }

        private boolean hasSectionName() {
            PsiBuilder.Marker marker = myBuilder.mark();
            IElementType next = myBuilder.lookAhead(1);
            IElementType nextNext = myBuilder.lookAhead(2);
            String tokenText = myBuilder.getTokenText();
            boolean res = next == DDocElementTypes.DDOC_COLON && !(tokenText != null && tokenText.contains(" "))
                // To reject `http://` eagerly
                && (nextNext == DDocElementTypes.DDOC_COMMENT_DATA || isBlankToken(nextNext));
            if (res) {
                myBuilder.advanceLexer(); // section name
                myBuilder.advanceLexer(); // `:`
                if (hasNewLine()) {
                    res = true;
                }
                else {
                    String text = myBuilder.getTokenText();
                    res = text != null && text.startsWith(" ");
                }
            }
            marker.rollbackTo();
            return res;
        }

        private String getSectionName() {
            //assert (hasSectionName())
            PsiBuilder.Marker marker = myBuilder.mark();
            String name = Objects.requireNonNull(myBuilder.getTokenText());
            myBuilder.advanceLexer(); // section name
            myBuilder.advanceLexer(); // `:`
            marker.done(DDocElementTypes.DDOC_SECTION_TITLE);
            return name;
        }

        private void parseSections() {
            consumeBlankLines();
            if (isEndOfComment()) {
                return;
            }
            parseSummary();
            consumeBlankLines();
            if (isEndOfComment()) {
                return;
            }
            parseDescription();
            consumeBlankLines();
            while (!isEndOfComment()) {
                parseNamedSection();
                consumeBlankLines();
            }
        }

        private void parseNamedSection() {
            PsiBuilder.Marker marker = myBuilder.mark();
            String sectionName = getSectionName();
            IElementType markerType = switch (sectionName.toUpperCase(Locale.ROOT)) {
                case "AUTHORS" -> DDocElementTypes.DDOC_AUTHORS_SECTION;
                case "BUGS" -> DDocElementTypes.DDOC_BUGS_SECTION;
                case "DATE" -> DDocElementTypes.DDOC_DATE_SECTION;
                case "DEPRECATED" -> DDocElementTypes.DDOC_DEPRECATED_SECTION;
                case "EXAMPLES" -> DDocElementTypes.DDOC_EXAMPLES_SECTION;
                case "HISTORY" -> DDocElementTypes.DDOC_HISTORY_SECTION;
                case "LICENSE" -> DDocElementTypes.DDOC_LICENSE_SECTION;
                case "RETURNS" -> DDocElementTypes.DDOC_RETURNS_SECTION;
                case "SEE_ALSO" -> DDocElementTypes.DDOC_SEE_ALSO_SECTION;
                case "STANDARDS" -> DDocElementTypes.DDOC_STANDARDS_SECTION;
                case "THROWS" -> DDocElementTypes.DDOC_THROWS_SECTION;
                case "VERSION" -> DDocElementTypes.DDOC_VERSION_SECTION;
                case "PARAMS" -> DDocElementTypes.DDOC_PARAMS_SECTION;
                case "MACROS" -> DDocElementTypes.DDOC_MACROS_SECTION;
                case "COPYRIGHT" -> DDocElementTypes.DDOC_COPYRIGHT_SECTION;
                default -> DDocElementTypes.DDOC_NAMED_SECTION;
            };
            if (markerType == DDocElementTypes.DDOC_MACROS_SECTION || markerType == DDocElementTypes.DDOC_PARAMS_SECTION) {
                parseSpecialSection(marker, markerType);
            }
            else {
                do {
                    parseParagraph();
                    consumeBlankLines();
                }
                while (!isEndOfComment() && !hasSectionName());
                marker.done(markerType);
            }
        }

        private void parseSummary() {
            if (hasSectionName()) {
                return;
            }
            PsiBuilder.Marker marker = myBuilder.mark();
            parseParagraph();
            marker.done(DDocElementTypes.DDOC_SUMMARY_SECTION);
        }

        private void parseDescription() {
            if (isEndOfComment() || hasSectionName()) {
                return;
            }
            PsiBuilder.Marker marker = myBuilder.mark();
            while (!isEndOfComment() && !hasSectionName()) {
                parseParagraph();
                consumeBlankLines();
            }
            marker.done(DDocElementTypes.DDOC_DESCRIPTION_SECTION);
        }

        private void parseParagraph() {
            PsiBuilder.Marker contentMarker = myBuilder.mark();
            boolean continueParagraph = true;
            while (!isEndOfComment() && !hasSectionName() && continueParagraph) {
                boolean stopSection = parseItem();
                if (stopSection) {
                    continueParagraph = false;
                }
            }
            contentMarker.done(DDocElementTypes.DDOC_SECTION_PARAGRAPH);
        }

        /**
         * Return: true if we reached the end of the section, false if we should continue
         */
        private boolean parseItem() {
            IElementType tokenType = getTokenType();
            if (tokenType == DDocElementTypes.DDOC_WHITESPACE) {
                boolean endOfParagraph = has2NewLine();
                consumeBlankLines();
                return endOfParagraph;
            }
            if (tokenType == DDOC_COMMENT_END) {
                myBuilder.advanceLexer();
            }
            else if (tokenType == DDocElementTypes.DDOC_COMMENT_DATA) {
                myBuilder.advanceLexer();
            }
            else if (tokenType == DDocElementTypes.DDOC_MACRO_OPEN) {
                parseMacroCall();
            }
            else if (tokenType == DDocElementTypes.DDOC_QUOTE_CHAR) {
                return parseQuote();
            }
            else if (tokenType == DDocElementTypes.DDOC_HEADING_CHARS) {
                parseHeading();
            }
            else if (tokenType == DDocElementTypes.DDOC_LEFT_BRACKET) {
                parseLink();
            }
            else if (tokenType == DDocElementTypes.DDOC_EXCLAMATION_MARK) {
                parseImage();
            }
            else if (tokenType == DDocElementTypes.DDOC_EMBEDDED_CODE_DELIMITER) {
                parseEmbeddedCode();
            }
            else if (tokenType == DDocElementTypes.DDOC_ORDERED_LIST_POINT) {
                parseOrderedList();
            }
            else if (tokenType == DDocElementTypes.DDOC_UNORDERED_LIST_POINT) {
                parseUnorderedList();
            }
            else if (tokenType == DDocElementTypes.DDOC_ID) {
                parseIdInNotParams();
            }
            else {
                myBuilder.advanceLexer();
            }
            return false;
        }

        private void parseMacroCall() {
            PsiBuilder.Marker marker = myBuilder.mark();
            assert myBuilder.getTokenType() == DDocElementTypes.DDOC_MACRO_OPEN;
            myBuilder.advanceLexer();
            while (!isEndOfComment() && myBuilder.getTokenType() != DDocElementTypes.DDOC_MACRO_END) {
                if (myBuilder.getTokenType() == DDocElementTypes.DDOC_MACRO_OPEN) {
                    parseMacroCall();
                }
                else {
                    myBuilder.advanceLexer();
                }
            }
            if (!isEndOfComment()) {
                myBuilder.advanceLexer();
            }
            marker.done(DDocElementTypes.DDOC_MACRO_CALL);
        }

        private boolean parseQuote() {
            PsiBuilder.Marker marker = myBuilder.mark();
            myBuilder.advanceLexer();
            // list, tables, embedded code, quote, etc. may be part of a quote
            boolean sectionEnd = false;
            while (!sectionEnd && !isEndOfComment()) {
                if (myBuilder.getTokenType() == DDocElementTypes.DDOC_QUOTE_CHAR) {
                    myBuilder.advanceLexer(); // skip this one as it’s a continuation quote char
                    String tokenText = myBuilder.getTokenText();
                    if (myBuilder.getTokenType() == DDocElementTypes.DDOC_WHITESPACE && !(tokenText != null && tokenText.indexOf('\n') >= 0)) {
                        // skip whitespace if next token on the same line to properly detect quote in quote
                        myBuilder.advanceLexer();
                    }
                }
                sectionEnd = parseItem();
            }
            marker.done(DDocElementTypes.DDOC_QUOTE);
            return sectionEnd;
        }

        private void parseLink() {
            if (!isBlankToken(myBuilder.rawLookup(-1)) && !rawLookupText(-1).toString().endsWith(" ")) {
                // no space before, it’s not a link (can be something like `int[Obj]`)
                myBuilder.advanceLexer();
                return;
            }
            parseLinkContent();
        }

        private boolean parseLinkContent() {
            PsiBuilder.Marker linkMarker = myBuilder.mark();
            PsiBuilder.Marker marker1 = myBuilder.mark();
            myBuilder.advanceLexer();
            while (!isEndOfComment() && (myBuilder.getTokenType() == DDocElementTypes.DDOC_WHITESPACE
                || myBuilder.getTokenType() == DDocElementTypes.DDOC_COMMENT_DATA
                || myBuilder.getTokenType() == DDocElementTypes.DDOC_QUOTE)) {
                if (myBuilder.getTokenType() == DDocElementTypes.DDOC_QUOTE) {
                    parseQuote();
                }
                else {
                    myBuilder.advanceLexer();
                }
            }
            if (myBuilder.getTokenType() != DDocElementTypes.DDOC_RIGHT_BRACKET) {
                // not a link (`[` but not `]`)
                marker1.drop();
                linkMarker.drop();
                myBuilder.advanceLexer();
                return false;
            }
            myBuilder.advanceLexer();
            IElementType afterText = myBuilder.getTokenType();
            if (afterText == DDocElementTypes.DDOC_LEFT_BRACKET || afterText == DDocElementTypes.DDOC_LEFT_PARENTHESES) {
                marker1.done(DDocElementTypes.DDOC_LINK_TEXT);
            }
            else if (afterText == DDocElementTypes.DDOC_COLON) {
                marker1.done(DDocElementTypes.DDOC_LINK_NAME);
            }
            else {
                marker1.done(DDocElementTypes.DDOC_LINK_REFERENCE_TO);
            }
            IElementType tokenType = myBuilder.getTokenType();
            if (tokenType == DDocElementTypes.DDOC_LEFT_BRACKET) {
                PsiBuilder.Marker markerReference = myBuilder.mark();
                myBuilder.advanceLexer();
                while (!isEndOfComment() && (myBuilder.getTokenType() == DDocElementTypes.DDOC_WHITESPACE
                    || myBuilder.getTokenType() == DDocElementTypes.DDOC_COMMENT_DATA)) {
                    myBuilder.advanceLexer();
                }
                if (myBuilder.getTokenType() != DDocElementTypes.DDOC_RIGHT_BRACKET) {
                    markerReference.rollbackTo();
                    linkMarker.done(DDocElementTypes.DDOC_LINK);
                    return true;
                }
                myBuilder.advanceLexer();
                markerReference.done(DDocElementTypes.DDOC_LINK_REFERENCE_TO);
                linkMarker.done(DDocElementTypes.DDOC_LINK);
            }
            else if (tokenType == DDocElementTypes.DDOC_LEFT_PARENTHESES) {
                PsiBuilder.Marker markerRawUrl = myBuilder.mark();
                myBuilder.advanceLexer();
                while (!isEndOfComment() && (myBuilder.getTokenType() == DDocElementTypes.DDOC_WHITESPACE
                    || myBuilder.getTokenType() == DDocElementTypes.DDOC_COMMENT_DATA
                    || myBuilder.getTokenType() == DDocElementTypes.DDOC_COLON)) {
                    myBuilder.advanceLexer();
                }
                if (myBuilder.getTokenType() != DDocElementTypes.DDOC_RIGHT_PARENTHESES) {
                    markerRawUrl.rollbackTo();
                    linkMarker.done(DDocElementTypes.DDOC_LINK);
                    return true;
                }
                myBuilder.advanceLexer();
                markerRawUrl.done(DDocElementTypes.DDOC_LINK_INLINE_REFERENCE_TEXT);
                linkMarker.done(DDocElementTypes.DDOC_LINK);
            }
            else if (tokenType == DDocElementTypes.DDOC_COLON) {
                myBuilder.advanceLexer();
                while (!isEndOfComment() && !hasNewLine()) {
                    myBuilder.advanceLexer();
                }
                linkMarker.done(DDocElementTypes.DDOC_LINK_DECLARATION);
            }
            else {
                linkMarker.done(DDocElementTypes.DDOC_LINK);
            }
            return true;
        }

        private void parseImage() {
            PsiBuilder.Marker marker = myBuilder.mark();
            myBuilder.advanceLexer();
            boolean wasLink = parseLinkContent();
            if (wasLink) {
                marker.done(DDocElementTypes.DDOC_IMAGE);
            }
            else {
                marker.drop();
            }
        }

        private void parseEmbeddedCode() {
            PsiBuilder.Marker marker = myBuilder.mark();
            myBuilder.advanceLexer();
            while (!isEndOfComment() && myBuilder.getTokenType() != DDocElementTypes.DDOC_EMBEDDED_CODE_DELIMITER) {
                myBuilder.advanceLexer();
            }
            if (!isEndOfComment()) {
                myBuilder.advanceLexer();
            }
            marker.done(DDocElementTypes.DDOC_EMBEDDED_CODE);
        }

        private void parseHeading() {
            assert myBuilder.getTokenType() == DDocElementTypes.DDOC_HEADING_CHARS;
            PsiBuilder.Marker marker = myBuilder.mark();
            myBuilder.advanceLexer();
            while (!isEndOfComment() && myBuilder.getTokenType() != DDocElementTypes.DDOC_HEADING_CHARS
                && myBuilder.getTokenType() != DDocElementTypes.DDOC_WHITESPACE) {
                myBuilder.advanceLexer();
            }
            if (!isEndOfComment() && myBuilder.getTokenType() != DDocElementTypes.DDOC_WHITESPACE) {
                myBuilder.advanceLexer();
            }
            marker.done(DDocElementTypes.DDOC_HEADING);
        }

        private void parseOrderedList() {
            assert myBuilder.getTokenType() == DDocElementTypes.DDOC_ORDERED_LIST_POINT;
            PsiBuilder.Marker marker = myBuilder.mark();
            int rootSpaceLevel = lastLineSpaceCount(rawLookupText(-1));
            while (!isEndOfComment() && !has2NewLine()) {
                consumeBlankLines();
                int spaceLevel = lastLineSpaceCount(rawLookupText(-1));
                if (spaceLevel < rootSpaceLevel
                    || myBuilder.getTokenType() != DDocElementTypes.DDOC_ORDERED_LIST_POINT) {
                    break;
                }
                assert myBuilder.getTokenType() == DDocElementTypes.DDOC_ORDERED_LIST_POINT;
                parseListContent(rootSpaceLevel);
            }
            marker.done(DDocElementTypes.DDOC_ORDERED_LIST);
        }

        private void parseUnorderedList() {
            PsiBuilder.Marker marker = myBuilder.mark();
            int rootSpaceLevel = lastLineSpaceCount(rawLookupText(-1));
            while (!isEndOfComment() && !has2NewLine()) {
                consumeBlankLines();
                int spaceLevel = lastLineSpaceCount(rawLookupText(-1));
                if (spaceLevel < rootSpaceLevel
                    || myBuilder.getTokenType() != DDocElementTypes.DDOC_UNORDERED_LIST_POINT) {
                    break;
                }
                assert myBuilder.getTokenType() == DDocElementTypes.DDOC_UNORDERED_LIST_POINT;
                parseListContent(rootSpaceLevel);
            }
            marker.done(DDocElementTypes.DDOC_UNORDERED_LIST);
        }

        /**
         * {@code text.split("\n").last().count { it == ' ' }}
         */
        private static int lastLineSpaceCount(CharSequence text) {
            String string = text.toString();
            String lastLine = string.substring(string.lastIndexOf('\n') + 1);
            int count = 0;
            for (int j = 0; j < lastLine.length(); j++) {
                if (lastLine.charAt(j) == ' ') {
                    count++;
                }
            }
            return count;
        }

        private void parseListContent(int rootSpaceLevel) {
            PsiBuilder.Marker m = myBuilder.mark();
            myBuilder.advanceLexer(); // 1. or +
            PsiBuilder.Marker contentMarker = myBuilder.mark();
            parseItem();
            while (!isEndOfComment() && !has2NewLine()) {
                consumeBlankLines();
                IElementType previousToken = myBuilder.rawLookup(-1);
                if (isBlankToken(previousToken) || previousToken == DDOC_COMMENT_LEADING_ASTERISKS) {
                    int spaceLevel = isBlankToken(previousToken) ? lastLineSpaceCount(rawLookupText(-1)) : 0;
                    if (spaceLevel <= rootSpaceLevel) {
                        break;
                    }
                }
                parseItem();
            }
            contentMarker.done(DDocElementTypes.DDOC_LIST_ITEM_CONTENT);
            m.done(DDocElementTypes.DDOC_LIST_ITEM);
        }

        private void parseIdInNotParams() {
            assert myBuilder.getTokenType() == DDocElementTypes.DDOC_ID;
            PsiBuilder.Marker m = myBuilder.mark();
            myBuilder.advanceLexer();
            if (myBuilder.getTokenType() == DDocElementTypes.DDOC_WHITESPACE) {
                myBuilder.advanceLexer();
            }
            assert myBuilder.getTokenType() == DDocElementTypes.DDOC_EQ;
            myBuilder.advanceLexer();
            m.collapse(DDocElementTypes.DDOC_COMMENT_DATA);
        }

        private void parseSpecialSection(PsiBuilder.Marker marker, IElementType markerType) {
            // In case we have some unwanted data
            while (!isEndOfComment() && !hasSectionName() && myBuilder.getTokenType() != DDocElementTypes.DDOC_ID) {
                parseItem();
            }
            while (!isEndOfComment() && !hasSectionName()) {
                assert myBuilder.getTokenType() == DDocElementTypes.DDOC_ID;
                PsiBuilder.Marker m = myBuilder.mark();
                myBuilder.advanceLexer();
                if (getTokenType() == DDocElementTypes.DDOC_WHITESPACE) {
                    myBuilder.advanceLexer();
                }
                assert myBuilder.getTokenType() == DDocElementTypes.DDOC_EQ;
                myBuilder.advanceLexer();
                while (!isEndOfComment() && !hasSectionName() && myBuilder.getTokenType() != DDocElementTypes.DDOC_ID) {
                    parseItem();
                    consumeBlankLines();
                }
                m.done(DDocElementTypes.DDOC_KEY_VALUE_ELEMENT);
            }
            marker.done(markerType);
        }
    }
}

package io.github.intellij.dlanguage.features;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.HighlighterIterator;
import consulo.document.Document;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.StringEscapesTokenTypes;
import consulo.language.editor.action.LanguageQuoteHandler;
import consulo.language.editor.action.MultiCharQuoteHandler;
import consulo.language.editor.action.SimpleTokenSetQuoteHandler;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.ALTERNATE_WYSIWYG_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.CHARACTER_LITERAL;
import static io.github.intellij.dlanguage.psi.DlangTypes.DELIMITED_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.DOUBLE_QUOTED_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.HEX_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.IES_END;
import static io.github.intellij.dlanguage.psi.DlangTypes.IES_START;
import static io.github.intellij.dlanguage.psi.DlangTypes.IES_TEXT;
import static io.github.intellij.dlanguage.psi.DlangTypes.TOKEN_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.WYSIWYG_STRING;

@ExtensionImpl
public class DQuoteHandler extends SimpleTokenSetQuoteHandler implements MultiCharQuoteHandler, LanguageQuoteHandler {

    public DQuoteHandler() {
        super(
            CHARACTER_LITERAL,
            DOUBLE_QUOTED_STRING,
            DELIMITED_STRING,
            WYSIWYG_STRING,
            ALTERNATE_WYSIWYG_STRING,
            TOKEN_STRING,
            HEX_STRING
        );
    }

    @Override
    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        int start = iterator.getStart();
        Object tokenType = iterator.getTokenType();
        if (tokenType == WYSIWYG_STRING || tokenType == DELIMITED_STRING || tokenType == TOKEN_STRING) {
            return offset - start <= 2;
        }
        if (tokenType == IES_START) {
            return true;
        }
        return super.isOpeningQuote(iterator, offset);
    }

    @Override
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
        boolean closingQuote = super.isClosingQuote(iterator, offset) || iterator.getTokenType() == IES_END;
        if (closingQuote) {
            // check escape next
            if (!iterator.atEnd()) {
                iterator.advance();
                if (!iterator.atEnd() && iterator.getTokenType() instanceof IElementType elementType
                    && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.contains(elementType)) {
                    closingQuote = false;
                }
                iterator.retreat();
            }
        }
        return closingQuote;
    }

    @Override
    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
        Object tokenType = iterator.getTokenType();
        if (!(tokenType instanceof IElementType elementType && myLiteralTokenSet.contains(elementType) || tokenType == IES_START)) {
            return false;
        }
        Document document = editor.getDocument();
        String text = document.getText();
        String quote = text.substring(iterator.getStart(), iterator.getStart() + 1);
        boolean hasCloseQuotes = StringUtil.contains(text.substring(offset + 1, iterator.getEnd()), quote);
        if (!hasCloseQuotes) {
            if (offset < 1) {
                return true;
            }
            // Special case when " is inserted before existing ending quote but after escape character \
            return text.charAt(offset - 1) != '\\';
        }
        // check if parser interpreted next text block start quotes as end quotes for the current one
        int nTextQuotes = StringUtil.getOccurrenceCount(text.substring(iterator.getEnd()), quote);
        int nTextEscapeQuotes = quote.equals("\"") ? StringUtil.getOccurrenceCount(text.substring(iterator.getEnd()), "\\\"") : 0;
        return (nTextQuotes - nTextEscapeQuotes) % 2 != 0;
    }

    @Override
    public @Nullable CharSequence getClosingQuote(HighlighterIterator iterator, int offset) {
        Object tokenType = iterator.getTokenType();
        if (tokenType == CHARACTER_LITERAL) {
            return "'";
        }
        if (tokenType == DOUBLE_QUOTED_STRING
            || tokenType == DELIMITED_STRING
            || tokenType == WYSIWYG_STRING
            || tokenType == IES_START
            || tokenType == HEX_STRING) {
            return "\"";
        }
        if (tokenType == ALTERNATE_WYSIWYG_STRING) {
            return "`";
        }
        if (tokenType == TOKEN_STRING) {
            return "}";
        }
        return null;
    }

    @Override
    public boolean isInsideLiteral(HighlighterIterator iterator) {
        return super.isInsideLiteral(iterator) || iterator.getTokenType() == IES_TEXT;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

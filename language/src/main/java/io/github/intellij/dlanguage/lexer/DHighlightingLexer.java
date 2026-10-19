package io.github.intellij.dlanguage.lexer;

import consulo.language.lexer.LayeredLexer;

import static io.github.intellij.dlanguage.psi.DlangTypes.CHARACTER_LITERAL;
import static io.github.intellij.dlanguage.psi.DlangTypes.DELIMITED_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.DOUBLE_QUOTED_STRING;
import static io.github.intellij.dlanguage.psi.DlangTypes.IES_TEXT;

/**
 * Second step highlight pass to handle errors in tokens (string contents for example)
 */
public class DHighlightingLexer extends LayeredLexer {
    public DHighlightingLexer() {
        super(new DlangLexer());
        registerLayer(new DLiteralEscapesLexer(CHARACTER_LITERAL), CHARACTER_LITERAL);
        registerLayer(new DLiteralEscapesLexer(DOUBLE_QUOTED_STRING), DOUBLE_QUOTED_STRING);
        registerLayer(new DDelimitedStringDelimiterLexer(), DELIMITED_STRING);
        registerLayer(new DLiteralEscapesLexer(IES_TEXT), IES_TEXT);
    }
}

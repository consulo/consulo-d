package io.github.intellij.dlanguage.features.documentation;

import consulo.language.ast.TokenSet;

import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_COMMENT_DATA;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_EMBEDDED_CODE_CONTENT;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_MACRO_ARGUMENT;
import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_WHITESPACE;

/**
 * Top-level declarations of upstream {@code DDocLexer.kt}.
 */
public final class DDocLexerKt {
    private static final TokenSet DDOC_TOKEN_MERGE =
        TokenSet.create(DDOC_COMMENT_DATA, DDOC_MACRO_ARGUMENT, DDOC_WHITESPACE, DDOC_EMBEDDED_CODE_CONTENT);

    private DDocLexerKt() {
    }

    public static TokenSet getDdocTokenMerge() {
        return DDOC_TOKEN_MERGE;
    }
}

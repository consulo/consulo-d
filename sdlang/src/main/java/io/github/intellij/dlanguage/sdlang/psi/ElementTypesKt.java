package io.github.intellij.dlanguage.sdlang.psi;

import consulo.language.ast.TokenSet;

import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.BLOCK_COMMENT;
import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.CHARACTER;
import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.LINE_COMMENT;
import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.RAW_STRING;
import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.REGULAR_STRING;

/**
 * Top-level declarations of upstream's {@code ElementTypes.kt}.
 */
public final class ElementTypesKt {
    public static final TokenSet SDLANG_COMMENTS = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT);
    public static final TokenSet SDLANG_STRINGS = TokenSet.create(RAW_STRING, REGULAR_STRING, CHARACTER);

    private ElementTypesKt() {
    }
}

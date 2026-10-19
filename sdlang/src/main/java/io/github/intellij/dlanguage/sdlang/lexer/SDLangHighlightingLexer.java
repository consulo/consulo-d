package io.github.intellij.dlanguage.sdlang.lexer;

import consulo.language.lexer.LayeredLexer;

public class SDLangHighlightingLexer extends LayeredLexer {
    public SDLangHighlightingLexer() {
        super(new SDLangLexer());
    }
}

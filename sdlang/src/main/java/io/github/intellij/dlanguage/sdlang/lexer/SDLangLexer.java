package io.github.intellij.dlanguage.sdlang.lexer;

import consulo.language.lexer.FlexAdapter;

import java.io.Reader;

public class SDLangLexer extends FlexAdapter {
    public SDLangLexer() {
        super(new _SDLangLexer((Reader) null));
    }
}

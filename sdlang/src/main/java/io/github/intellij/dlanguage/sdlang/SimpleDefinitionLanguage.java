package io.github.intellij.dlanguage.sdlang;

import consulo.language.Language;

public final class SimpleDefinitionLanguage extends Language {
    public static final SimpleDefinitionLanguage INSTANCE = new SimpleDefinitionLanguage();

    private SimpleDefinitionLanguage() {
        super("SDLang");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}

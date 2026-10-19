import org.jspecify.annotations.NullMarked;

/**
 * SDLang (Simple Declarative Language), the alternative format of DUB package recipes.
 */
@NullMarked
module consulo.d.sdlang {
    requires consulo.d.base;
    requires consulo.language.api;
    requires consulo.language.impl;
    requires consulo.language.editor.api;

    exports consulo.d.sdlang;
    exports io.github.intellij.dlanguage.sdlang;
    exports io.github.intellij.dlanguage.sdlang.annotator;
    exports io.github.intellij.dlanguage.sdlang.colors;
    exports io.github.intellij.dlanguage.sdlang.editor;
    exports io.github.intellij.dlanguage.sdlang.highlighting;
    exports io.github.intellij.dlanguage.sdlang.impl;
    exports io.github.intellij.dlanguage.sdlang.lexer;
    exports io.github.intellij.dlanguage.sdlang.parser;
    exports io.github.intellij.dlanguage.sdlang.psi;
    exports io.github.intellij.dlanguage.sdlang.psi.impl;
}

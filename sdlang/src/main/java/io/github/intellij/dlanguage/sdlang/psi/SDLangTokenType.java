package io.github.intellij.dlanguage.sdlang.psi;

import consulo.language.ast.IElementType;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;

public class SDLangTokenType extends IElementType {
    public SDLangTokenType(String debugName) {
        super(debugName, SimpleDefinitionLanguage.INSTANCE);
    }
}

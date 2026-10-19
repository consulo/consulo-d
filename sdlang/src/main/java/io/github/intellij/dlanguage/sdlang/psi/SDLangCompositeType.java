package io.github.intellij.dlanguage.sdlang.psi;

import consulo.language.ast.IElementType;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;

public class SDLangCompositeType extends IElementType {
    public SDLangCompositeType(String debugName) {
        super(debugName, SimpleDefinitionLanguage.INSTANCE);
    }
}

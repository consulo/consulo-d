package io.github.intellij.dlanguage.sdlang.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.action.LanguageQuoteHandler;
import consulo.language.editor.action.SimpleTokenSetQuoteHandler;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;

import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.CHARACTER;
import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.RAW_STRING;
import static io.github.intellij.dlanguage.sdlang.psi.SDLangElementTypes.REGULAR_STRING;

@ExtensionImpl
public class SDLangQuoteHandler extends SimpleTokenSetQuoteHandler implements LanguageQuoteHandler {
    public SDLangQuoteHandler() {
        super(REGULAR_STRING, RAW_STRING, CHARACTER);
    }

    @Override
    public Language getLanguage() {
        return SimpleDefinitionLanguage.INSTANCE;
    }
}

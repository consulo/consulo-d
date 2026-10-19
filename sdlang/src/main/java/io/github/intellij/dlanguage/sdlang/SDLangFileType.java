package io.github.intellij.dlanguage.sdlang;

import consulo.d.localize.DLocalize;
import consulo.language.file.LanguageFileType;
import consulo.language.plain.PlainTextFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

public final class SDLangFileType extends LanguageFileType {
    public static final SDLangFileType INSTANCE = new SDLangFileType();

    private SDLangFileType() {
        super(SimpleDefinitionLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "SDLang";
    }

    @Override
    public String getDefaultExtension() {
        return "sdl";
    }

    @Override
    public LocalizeValue getDescription() {
        return DLocalize.sdlangFiletype();
    }

    @Override
    public Image getIcon() {
        return PlainTextFileType.INSTANCE.getIcon();
    }
}

package io.github.intellij.dlanguage.sdlang.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class SDLangCommenter implements Commenter {
    @Override
    public String getLineCommentPrefix() {
        return "//";
    }

    @Override
    public String getBlockCommentPrefix() {
        return "/*";
    }

    @Override
    public String getBlockCommentSuffix() {
        return "*/";
    }

    @Override
    public @Nullable String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Override
    public @Nullable String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Override
    public Language getLanguage() {
        return SimpleDefinitionLanguage.INSTANCE;
    }
}

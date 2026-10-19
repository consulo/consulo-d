package io.github.intellij.dlanguage.sdlang.highlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class SDLangHighlighterFactory extends SyntaxHighlighterFactory {
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
        return new SDLangHighlighter();
    }

    @Override
    public Language getLanguage() {
        return SimpleDefinitionLanguage.INSTANCE;
    }
}

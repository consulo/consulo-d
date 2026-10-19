package io.github.intellij.dlanguage.highlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class DHighlighterFactory extends SyntaxHighlighterFactory {
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
        return new DHighlighter();
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

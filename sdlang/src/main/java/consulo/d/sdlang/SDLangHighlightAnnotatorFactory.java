package consulo.d.sdlang;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;
import io.github.intellij.dlanguage.sdlang.annotator.SDLangHighlightAnnotator;
import org.jspecify.annotations.Nullable;

/**
 * Registers {@link SDLangHighlightAnnotator}; upstream did it in {@code sdlang.xml}
 * ({@code <annotator language="SDLang" implementationClass="...SDLangHighlightAnnotator"/>}).
 */
@ExtensionImpl
public class SDLangHighlightAnnotatorFactory implements AnnotatorFactory {
    @Override
    public @Nullable Annotator createAnnotator() {
        return new SDLangHighlightAnnotator();
    }

    @Override
    public Language getLanguage() {
        return SimpleDefinitionLanguage.INSTANCE;
    }
}

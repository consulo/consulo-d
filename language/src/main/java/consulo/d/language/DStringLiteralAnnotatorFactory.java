package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.annotator.DStringLiteralAnnotator;
import org.jspecify.annotations.Nullable;

/**
 * Registers {@link DStringLiteralAnnotator}; upstream did it in {@code plugin.xml}
 * ({@code <annotator language="D" implementationClass="...DStringLiteralAnnotator"/>}).
 */
@ExtensionImpl
public class DStringLiteralAnnotatorFactory implements AnnotatorFactory {
    @Override
    public @Nullable Annotator createAnnotator() {
        return new DStringLiteralAnnotator();
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.annotator.DUnclosedCommentAnnotator;
import org.jspecify.annotations.Nullable;

/**
 * Registers {@link DUnclosedCommentAnnotator}; upstream did it in {@code plugin.xml}
 * ({@code <annotator language="D" implementationClass="...DUnclosedCommentAnnotator"/>}).
 */
@ExtensionImpl
public class DUnclosedCommentAnnotatorFactory implements AnnotatorFactory {
    @Override
    public @Nullable Annotator createAnnotator() {
        return new DUnclosedCommentAnnotator();
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

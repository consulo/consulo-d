package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.annotator.DlangSyntaxAnnotator;
import org.jspecify.annotations.Nullable;

/**
 * Registers {@link DlangSyntaxAnnotator}; upstream did it in {@code plugin.xml}
 * ({@code <annotator language="D" implementationClass="...DlangSyntaxAnnotator"/>}).
 */
@ExtensionImpl
public class DlangSyntaxAnnotatorFactory implements AnnotatorFactory {
    @Override
    public @Nullable Annotator createAnnotator() {
        return new DlangSyntaxAnnotator();
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

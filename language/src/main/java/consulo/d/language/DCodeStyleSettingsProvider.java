package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import consulo.language.codeStyle.setting.LanguageCodeStyleSettingsProvider;
import io.github.intellij.dlanguage.DLanguage;

import java.util.Objects;

/**
 * The D code style page. Consulo builds code style pages from {@code CodeStyleSettingsProvider} extensions only; the
 * page itself is upstream's, made by
 * {@link io.github.intellij.dlanguage.features.formatter.settings.DLanguageCodeStyleSettingsProvider}.
 */
@ExtensionImpl
public class DCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return Objects.requireNonNull(LanguageCodeStyleSettingsProvider.forLanguage(DLanguage.INSTANCE))
            .createSettingsPage(settings, originalSettings);
    }
}

package io.github.intellij.dlanguage.features.formatter;

import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.CustomCodeStyleSettings;

@SuppressWarnings("PropertyName")
public class DCodeStyleSettings extends CustomCodeStyleSettings {

    public boolean SPACE_BEFORE_IMPORT_BINDS_COLON = true;

    public DCodeStyleSettings(CodeStyleSettings container) {
        super(DCodeStyleSettings.class.getSimpleName(), container);
    }
}

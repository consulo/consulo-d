package io.github.intellij.dlanguage.features.formatter.settings;

import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.ui.setting.TabbedLanguageCodeStylePanel;
import consulo.ui.annotation.RequiredUIAccess;
import io.github.intellij.dlanguage.DLanguage;

public class DCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

    @RequiredUIAccess
    public DCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
        super(DLanguage.INSTANCE, currentSettings, settings);
    }

    @Override
    @RequiredUIAccess
    protected void initTabs(CodeStyleSettings settings) {
        super.initTabs(settings);
    }
}

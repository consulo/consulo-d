package io.github.intellij.dlanguage.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.execution.configuration.RunConfiguration;
import consulo.project.Project;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class DlangRunDmdConfigurationType extends ConfigurationTypeBase {

    public static DlangRunDmdConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(DlangRunDmdConfigurationType.class);
    }

    public DlangRunDmdConfigurationType() {
        super("DlangRunDmdConfigType",
            DLocalize.runDmdText(),
            DLocalize.runDmdDescr(),
            DLanguage.Icons.RUN);
        addFactory(new DLanguageFactory(this));
    }

    private static class DLanguageFactory extends DlangRunConfigurationFactory {
        public DLanguageFactory(final ConfigurationType type) {
            super(type);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(final Project project) {
            return new DlangRunDmdConfiguration("DlangRunDmdConfig", project, this);
        }
    }
}

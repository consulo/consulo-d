package io.github.intellij.dlanguage.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.execution.BeforeRunTask;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.execution.configuration.RunConfiguration;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class DlangRunAppConfigurationType extends ConfigurationTypeBase {

    public static DlangRunAppConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(DlangRunAppConfigurationType.class);
    }

    public DlangRunAppConfigurationType() {
        super("DlangRunAppConfigType",
            DLocalize.runAppText(),
            DLocalize.runAppDescr(),
            DLanguage.Icons.RUN);
        addFactory(new DLanguageFactory(this));
    }

    private static class DLanguageFactory extends DlangRunConfigurationFactory {
        public DLanguageFactory(final ConfigurationType type) {
            super(type);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(final Project project) {
            return new DlangRunAppConfiguration("DlangRunAppConfig", project, this);
        }

        /**
         * Controls the default 'before launch' tasks that are configured in a run configuration
         *
         * @param providerID the key of the Task to check
         * @param task the actual Task
         */
        @Override
        public void configureBeforeRunTaskDefaults(Key<? extends BeforeRunTask> providerID, BeforeRunTask task) {
            // potentially we could enable DlangRunDmdConfigurationType.ID
            // to do that we'll need to change the overridden boolean values in DlangRunAppConfiguration and
            // also add a <stepsBeforeRunProvider implementation="DlangCompileBeforeRunProvider" id="dlangCompileBeforeRun"/>
            // to the plugin.xml and creating a BeforeRunTask which compiles the project.
            // see: https://stackoverflow.com/a/53979347/912408
        }
    }

}

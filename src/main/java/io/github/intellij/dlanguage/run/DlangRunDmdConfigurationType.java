package io.github.intellij.dlanguage.run;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import consulo.d.DlangBundle;
import io.github.intellij.dlanguage.icons.DlangIcons;

public class DlangRunDmdConfigurationType implements ConfigurationType {
    private final DLanguageFactory myConfigurationFactory;

    public DlangRunDmdConfigurationType() {
        myConfigurationFactory = new DLanguageFactory(this);
    }

    @Override
    public String getDisplayName() {
        return DlangBundle.INSTANCE.message("run.dmd.text");
    }

    @Override
    public String getConfigurationTypeDescription() {
        return DlangBundle.INSTANCE.message("run.dmd.descr");
    }

    @Override
    public Icon getIcon() {
        return DlangIcons.FILE;
    }

    @NotNull
    @Override
    public String getId() {
        return "DlangRunDmdConfiguration";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myConfigurationFactory};
    }

    private static class DLanguageFactory extends ConfigurationFactory {
        public DLanguageFactory(final ConfigurationType type) {
            super(type);
        }

        public RunConfiguration createTemplateConfiguration(final Project project) {
            return new DlangRunDmdConfiguration("Compile with DMD", project, this);
        }
    }
}

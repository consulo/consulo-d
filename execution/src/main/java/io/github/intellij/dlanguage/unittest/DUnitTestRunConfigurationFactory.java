package io.github.intellij.dlanguage.unittest;

import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.RunConfiguration;
import consulo.project.Project;
import io.github.intellij.dlanguage.run.DlangRunConfigurationFactory;

public class DUnitTestRunConfigurationFactory extends DlangRunConfigurationFactory {

    /**
     * Constructed by DUnitTestRunConfigurationType
     *
     * @param type ConfigurationType (the DUnitTestRunConfigurationType that creates this factory)
     */
    protected DUnitTestRunConfigurationFactory(final ConfigurationType type) {
        super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(final Project project) {
        return new DUnitTestRunConfiguration("DlangTestConfig", project);
    }
}

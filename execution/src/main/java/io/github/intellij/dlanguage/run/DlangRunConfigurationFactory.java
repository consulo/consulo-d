package io.github.intellij.dlanguage.run;

import consulo.execution.BeforeRunTask;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationType;
import consulo.project.Project;
import consulo.util.dataholder.Key;

/**
 * Base ConfigurationType class for D language configuration types that are used for running dub, dmd, or run a compiled binary
 * <p>
 * Consulo: a {@link ConfigurationFactory} of the given type. Upstream extended IntelliJ's {@code SimpleConfigurationType},
 * which is its own type in Consulo too, so the configurations would not belong to the registered type. Consulo's
 * {@code RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY} is {@link #isConfigurationSingletonByDefault()}.
 *
 * @author Samael Bate (singingbush)
 * created on 11/10/2020
 */
public abstract class DlangRunConfigurationFactory extends ConfigurationFactory {

    public DlangRunConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public boolean isApplicable(Project project) {
        return true; // todo: only return true if this project is using D language
    }

    @Override
    public boolean isConfigurationSingletonByDefault() {
        return true; // so can't be run more than once at same time
    }

    /**
     * Controls the default 'before launch' tasks that are configured in a run configuration
     * <p>
     * Implemented this method so that we could suppress the /out directory being built:
     * <a href="https://github.com/intellij-dlanguage/intellij-dlanguage/issues/489">issue 489</a>
     * <a href="https://github.com/intellij-dlanguage/intellij-dlanguage/issues/1215">issue 1215</a>
     * </p>
     *
     * @param providerID the key of the Task to check
     * @param task the actual Task
     */
    @Override
    public void configureBeforeRunTaskDefaults(Key<? extends BeforeRunTask> providerID, BeforeRunTask task) {
        // by default, don't have any of our run configurations setup tasks.
        // potentially the Run App config could override this to do a compile before running the binary
        task.setEnabled(false);
    }
}

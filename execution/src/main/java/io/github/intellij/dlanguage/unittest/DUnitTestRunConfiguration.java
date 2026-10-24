package io.github.intellij.dlanguage.unittest;

import consulo.d.localize.DLocalize;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.EnvironmentAware;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.LocatableConfigurationBase;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.WorkingDirectoryAware;
import consulo.execution.configuration.fragment.EnvironmentFragmentState;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.WorkingDirectoryFragmentState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.run.DUnitTestRunConfigurationOptions;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Consulo: the configuration belongs to the registered factory of {@link DUnitTestRunConfigurationType} (upstream
 * created a new factory for every configuration). The working directory and the environment are the shared fragments;
 * old configurations stored the fields as upstream's {@code XmlSerializer} wrote them
 * ({@code <option name=".." value=".."/>}, the environment as a map).
 */
public class DUnitTestRunConfiguration extends LocatableConfigurationBase implements WorkingDirectoryAware, EnvironmentAware {

    public DUnitTestRunConfiguration(final Project project) {
        this(DLanguage.INSTANCE.getDisplayName().get(), project);
    }

    public DUnitTestRunConfiguration(String name, Project project) {
        super(project, DUnitTestRunConfigurationType.getInstance().getConfigurationFactories()[0], name);
    }

    @Override
    protected Class<DUnitTestRunConfigurationOptions> getOptionsClass() {
        return DUnitTestRunConfigurationOptions.class;
    }

    @Override
    public DUnitTestRunConfigurationOptions getOptions() {
        return (DUnitTestRunConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(final LegacyRunConfigurationConverter converter) {
        final DUnitTestRunConfigurationLegacyState old = converter.readXml(DUnitTestRunConfigurationLegacyState.class);

        final DUnitTestRunConfigurationOptions options = new DUnitTestRunConfigurationOptions();
        options.setDFilePath(old.dFilePath);
        converter.setOptions(options);

        converter.addFragment(new WorkingDirectoryFragmentState(old.workingDir));
        converter.addFragment(new EnvironmentFragmentState(old.envVars, true));
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return List.of(new DUnitTestFileFragment());
    }

    public @Nullable String getdFilePath() {
        return getOptions().getDFilePath();
    }

    public void setdFilePath(final @Nullable String dFilePath) {
        getOptions().setDFilePath(dFilePath);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException
    {
        // TODO: Fill this in
    }

    @Override
    public RunProfileState getState(final Executor executor, final ExecutionEnvironment environment) throws ExecutionException {
        return new DUnitTestRunProfileState(environment);
    }

    public VirtualFile getDFile() throws RuntimeConfigurationError {
        final String filePath = getdFilePath();
        if (StringUtil.isEmptyOrSpaces(filePath)) {
            throw new RuntimeConfigurationError(DLocalize.pathToDlanguageFileNotSet());
        }

        final VirtualFile dFile = LocalFileSystem.getInstance().findFileByPath(filePath);
        if (dFile == null || dFile.isDirectory()) {
            throw new RuntimeConfigurationError(DLocalize.dlanguageFileNotFound(FileUtil.toSystemDependentName(filePath)));
        }

        if (dFile.getFileType() != DlangFileType.INSTANCE) {
            throw new RuntimeConfigurationError(DLocalize.notADlanguageFile(FileUtil.toSystemDependentName(filePath)));
        }

        return dFile;
    }

    @Override
    public @Nullable String suggestedName() {
        final String filePath = getdFilePath();
        return filePath == null ? null : PathUtil.getFileName(filePath);
    }
//
//    public DUnitTestRunConfiguration clone() {
//        final DUnitTestRunConfiguration clone = (DUnitTestRunConfiguration)super.clone();
//        clone.myRunnerParameters = myRunnerParameters.clone();
//        return clone;
//    }
}

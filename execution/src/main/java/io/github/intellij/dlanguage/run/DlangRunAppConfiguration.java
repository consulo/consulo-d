package io.github.intellij.dlanguage.run;

import consulo.compiler.ModuleCompilerPathsManager;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunProfileWithCompileBeforeLaunchOption;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.run.NativeProgramRunConfiguration;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import io.github.intellij.dlanguage.run.exception.ModuleNotFoundException;
import io.github.intellij.dlanguage.run.exception.NoValidDlangSdkFound;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Consulo: the working directory, the program parameters and the environment are the shared program fragments
 * (upstream's {@code workDir}, {@code additionalParams} and {@code envVars}), which Debug launches the executable with
 * under the nativeDev debugger (upstream's {@code RunAppRunner}). IntelliJ's {@code isBuildBeforeLaunchAddedByDefault} and
 * {@code isExcludeCompileBeforeLaunchOption} have no Consulo counterpart.
 */
public class DlangRunAppConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
    implements NativeProgramRunConfiguration, RunProfileWithCompileBeforeLaunchOption {

    public DlangRunAppConfiguration(final String name,
                                    final Project project,
                                    final ConfigurationFactory factory) {
        super(name, new RunConfigurationModule(project), factory);

        final Collection<Module> modules = this.getValidModules();
        if (!modules.isEmpty()) {
            //Get first valid module and use it
            this.setModule(modules.iterator().next());
        }

        setWorkingDirectory(project.getBasePath());
    }

    public String getExecutablePath() {
        final @Nullable Module module = getConfigurationModule().getModule();
        if (module != null && !module.isDisposed()) {
            final String outputPath = ModuleCompilerPathsManager.getInstance(module)
                                                        .getCompilerOutputUrl(ProductionContentFolderTypeProvider.getInstance());

            String filename = module.getName();
            if (Platform.current().os().isWindows()) {
                filename += ".exe";
            }
            final File outputFile = new File(VirtualFileUtil.urlToPath(outputPath), filename);
            return outputFile.getPath();
        } else {
            return "";
        }
    }

    @Override
    public Collection<Module> getValidModules() {
        final Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        final DMDRunner appRunner = new DMDRunner();

        final ArrayList<Module> res = new ArrayList<>();
        for (final Module module : modules) {
            if (appRunner.isValidModule(module)) {
                res.add(module);
            }
        }
        return res;
    }

    @Override
    protected Class<DlangRunAppConfigurationOptions> getOptionsClass() {
        return DlangRunAppConfigurationOptions.class;
    }

    @Override
    public DlangRunAppConfigurationOptions getOptions() {
        return (DlangRunAppConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(final LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        converter.setOptions(new DlangRunAppConfigurationOptions());

        NativeProgramRunConfiguration.convertLegacyProgramState(converter);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return List.of(new DlangRunAppExecutableFragment());
    }

    @Override
    public RunProfileState getState(final Executor executor, final ExecutionEnvironment env) throws ExecutionException {
        return new DlangRunAppState(env, this);
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        try {
            GeneralCommandLine cmd = new DlangRunAppState(environment, this).getExecutableCommandLine(this);
            File workDirectory = cmd.getWorkDirectory();
            return createDebugTarget(Path.of(cmd.getExePath()), workDirectory == null ? null : workDirectory.toPath());
        }
        catch (ModuleNotFoundException e) {
            throw new ExecutionException(DLocalize.runNoModuleSelected().get());
        }
        catch (NoValidDlangSdkFound e) {
            throw new ExecutionException(DLocalize.runAppNoValidSdk().get());
        }
    }

    @Override
    public boolean isBuildProjectOnEmptyModuleList() {
        return true;
    }
}

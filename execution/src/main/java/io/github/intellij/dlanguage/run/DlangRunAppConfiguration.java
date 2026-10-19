package io.github.intellij.dlanguage.run;

import consulo.compiler.ModuleCompilerPathsManager;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunProfileWithCompileBeforeLaunchOption;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.run.NativeProgramRunConfiguration;
import consulo.nativeDev.debugger.run.NativeProgramSettings;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import io.github.intellij.dlanguage.run.exception.ModuleNotFoundException;
import io.github.intellij.dlanguage.run.exception.NoValidDlangSdkFound;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Consulo: the working directory, the program parameters and the environment are nativeDev's
 * {@link NativeProgramSettings} (upstream's {@code workDir}, {@code additionalParams} and {@code envVars} accessors
 * delegate to them), which Debug launches the executable with under the nativeDev debugger (upstream's
 * {@code RunAppRunner}). IntelliJ's {@code isBuildBeforeLaunchAddedByDefault} and
 * {@code isExcludeCompileBeforeLaunchOption} have no Consulo counterpart.
 */
public class DlangRunAppConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
    implements NativeProgramRunConfiguration, RunProfileWithCompileBeforeLaunchOption {

    private final NativeProgramSettings programSettings = new NativeProgramSettings();

    public DlangRunAppConfiguration(final String name,
                                    final Project project,
                                    final ConfigurationFactory factory) {
        super(name, new RunConfigurationModule(project), factory);

        final Collection<Module> modules = this.getValidModules();
        if (!modules.isEmpty()) {
            //Get first valid module and use it
            this.setModule(modules.iterator().next());
        }

        setWorkDir(project.getBasePath());
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
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DlangRunAppConfigurationEditor(getProject());
    }

    @Override
    public RunProfileState getState(final Executor executor, final ExecutionEnvironment env) throws ExecutionException {
        return new DlangRunAppState(env, this);
    }

    @Override
    public NativeProgramSettings getProgramSettings() {
        return programSettings;
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        try {
            GeneralCommandLine cmd = new DlangRunAppState(environment, this).getExecutableCommandLine(this);
            File workDirectory = cmd.getWorkDirectory();
            return programSettings.createDebugTarget(Path.of(cmd.getExePath()), workDirectory == null ? null : workDirectory.toPath());
        }
        catch (ModuleNotFoundException e) {
            throw new ExecutionException(DLocalize.runNoModuleSelected().get());
        }
        catch (NoValidDlangSdkFound e) {
            throw new ExecutionException(DLocalize.runAppNoValidSdk().get());
        }
    }

    @Override
    public void writeExternal(final Element element) throws WriteExternalException {
        super.writeExternal(element);
        programSettings.writeExternal(element);
    }

    @Override
    public void readExternal(final Element element) throws InvalidDataException {
        super.readExternal(element);
        programSettings.readExternal(element);
    }

    /* Getters and Setters. Autogenerated by IntelliJ IDEA */
    public @Nullable String getWorkDir() {
        return programSettings.getWorkingDirectory();
    }

    public void setWorkDir(final @Nullable String workDir) {
        programSettings.setWorkingDirectory(workDir);
    }

    public @Nullable String getAdditionalParams() {
        return programSettings.getProgramParameters();
    }

    public void setAdditionalParams(final @Nullable String additionalParams) {
        programSettings.setProgramParameters(additionalParams);
    }

    public Map<String, String> getEnvVars() {
        return programSettings.getEnvs();
    }

    public void setEnvVars(final Map<String, String> envVars) {
        programSettings.setEnvs(envVars);
    }

    @Override
    public boolean isBuildProjectOnEmptyModuleList() {
        return true;
    }
}

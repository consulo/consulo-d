package io.github.intellij.dub.run;

import consulo.d.dub.DubUtil;
import consulo.d.execution.fragment.DTagRunConfigurationFragment;
import consulo.d.execution.fragment.DTextRunConfigurationFragment;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunProfileWithCompileBeforeLaunchOption;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.run.NativeProgramRunConfiguration;
import consulo.process.ExecutionException;
import consulo.project.Project;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Port of upstream {@code DlangRunDubConfiguration.kt}. The working directory, the environment and the debugger come
 * from the shared native program fragments; the program arguments are passed after {@code --} to {@code dub run}.
 * Debugging launches the executable the root package builds to, as the last DUB import found it.
 */
public class DlangRunDubConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
    implements NativeProgramRunConfiguration, RunProfileWithCompileBeforeLaunchOption {

    public DlangRunDubConfiguration(String name, Project project, ConfigurationFactory factory) {
        super(name, new RunConfigurationModule(project), factory);
        Collection<Module> modules = getValidModules();
        if (!modules.isEmpty()) {
            //Get first valid module and use it
            setModule(modules.iterator().next());
        }
    }

    @Override
    protected Class<DlangRunDubConfigurationOptions> getOptionsClass() {
        return DlangRunDubConfigurationOptions.class;
    }

    @Override
    public DlangRunDubConfigurationOptions getOptions() {
        return (DlangRunDubConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        Element element = converter.getElement();
        DlangRunDubConfigurationOptions options = new DlangRunDubConfigurationOptions();
        options.setGeneralDubOptions(readInt(element, "generalDubOptions", 1));
        options.setCbRdmd(readBoolean(element, "cbRdmd"));
        options.setTfBuild(element.getAttributeValue("tfBuild"));
        options.setTfCompiler(element.getAttributeValue("tfCompiler"));
        options.setTfConfig(element.getAttributeValue("tfConfig"));
        options.setTfArch(element.getAttributeValue("tfArch"));
        options.setTfDebug(element.getAttributeValue("tfDebug"));
        options.setBuildMode(readInt(element, "buildMode", 0));
        options.setCbForce(readBoolean(element, "cbForce"));
        options.setCbNoDeps(readBoolean(element, "cbNoDeps"));
        options.setCbForceRemove(readBoolean(element, "cbForceRemove"));
        options.setCbCombined(readBoolean(element, "cbCombined"));
        options.setCbParallel(readBoolean(element, "cbParallel"));
        options.setCbTempBuild(readBoolean(element, "cbTempBuild"));
        options.setTfMainFile(element.getAttributeValue("tfMainFile"));
        options.setCbCoverage(readBoolean(element, "cbCoverage"));
        options.setQuiet(readBoolean(element, "quiet"));
        options.setVerbose(readBoolean(element, "verbose"));
        options.setAdditionalParams(element.getAttributeValue("additionalParams"));
        converter.setOptions(options);

        NativeProgramRunConfiguration.convertLegacyProgramState(converter);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return List.of(
            new DlangDubCommandFragment(),
            text("dub.build", DLocalize.runConfigurationFragmentDubBuild(), DLocalize.dubRunBuildLabel(),
                DlangRunDubConfiguration::getTfBuild, DlangRunDubConfiguration::setTfBuild),
            text("dub.config", DLocalize.runConfigurationFragmentDubConfig(), DLocalize.dubRunConfigLabel(),
                DlangRunDubConfiguration::getTfConfig, DlangRunDubConfiguration::setTfConfig),
            text("dub.arch", DLocalize.runConfigurationFragmentDubArch(), DLocalize.dubRunArchLabel(),
                DlangRunDubConfiguration::getTfArch, DlangRunDubConfiguration::setTfArch),
            text("dub.debug", DLocalize.runConfigurationFragmentDubDebug(), DLocalize.dubRunDebugLabel(),
                DlangRunDubConfiguration::getTfDebug, DlangRunDubConfiguration::setTfDebug),
            text("dub.compiler", DLocalize.runConfigurationFragmentDubCompiler(), DLocalize.dubRunCompilerLabel(),
                DlangRunDubConfiguration::getTfCompiler, DlangRunDubConfiguration::setTfCompiler),
            new DlangDubBuildModeFragment(),
            text("dub.mainFile", DLocalize.runConfigurationFragmentDubMainFile(), DLocalize.dubRunMainFileLabel(),
                DlangRunDubConfiguration::getTfMainFile, DlangRunDubConfiguration::setTfMainFile),
            text("dub.arguments", DLocalize.runConfigurationFragmentDubArguments(), DLocalize.dubRunParametersLabel(),
                DlangRunDubConfiguration::getAdditionalParams, DlangRunDubConfiguration::setAdditionalParams),
            tag("dub.rdmd", DLocalize.dubRunOptionRdmd(), DlangRunDubConfiguration::isCbRdmd, DlangRunDubConfiguration::setCbRdmd),
            tag("dub.force", DLocalize.dubRunOptionForce(), DlangRunDubConfiguration::isCbForce, DlangRunDubConfiguration::setCbForce),
            tag("dub.nodeps", DLocalize.dubRunOptionNodeps(), DlangRunDubConfiguration::isCbNoDeps, DlangRunDubConfiguration::setCbNoDeps),
            tag("dub.forceRemove", DLocalize.dubRunOptionForceRemove(),
                DlangRunDubConfiguration::isCbForceRemove, DlangRunDubConfiguration::setCbForceRemove),
            tag("dub.combined", DLocalize.dubRunOptionCombined(),
                DlangRunDubConfiguration::isCbCombined, DlangRunDubConfiguration::setCbCombined),
            tag("dub.parallel", DLocalize.dubRunOptionParallel(),
                DlangRunDubConfiguration::isCbParallel, DlangRunDubConfiguration::setCbParallel),
            tag("dub.tempBuild", DLocalize.dubRunOptionTempBuild(),
                DlangRunDubConfiguration::isCbTempBuild, DlangRunDubConfiguration::setCbTempBuild),
            tag("dub.coverage", DLocalize.dubRunOptionCoverage(),
                DlangRunDubConfiguration::isCbCoverage, DlangRunDubConfiguration::setCbCoverage),
            tag("dub.quiet", DLocalize.dubRunOptionQuiet(), DlangRunDubConfiguration::isQuiet, DlangRunDubConfiguration::setQuiet),
            tag("dub.verbose", DLocalize.dubRunOptionVerbose(), DlangRunDubConfiguration::isVerbose, DlangRunDubConfiguration::setVerbose)
        );
    }

    private static DTextRunConfigurationFragment<DlangRunDubConfiguration> text(String id,
                                                                               LocalizeValue name,
                                                                               LocalizeValue label,
                                                                               Function<DlangRunDubConfiguration, @Nullable String> getter,
                                                                               BiConsumer<DlangRunDubConfiguration, @Nullable String> setter) {
        return new DTextRunConfigurationFragment<>(id, name, label, getter, setter).withGroup(DLocalize.runConfigurationFragmentGroupDub());
    }

    private static DTagRunConfigurationFragment<DlangRunDubConfiguration> tag(String id,
                                                                             LocalizeValue name,
                                                                             Predicate<DlangRunDubConfiguration> getter,
                                                                             BiConsumer<DlangRunDubConfiguration, Boolean> setter) {
        return new DTagRunConfigurationFragment<>(id, name, getter, setter).withGroup(DLocalize.runConfigurationFragmentGroupDub());
    }

    @Override
    public Collection<Module> getValidModules() {
        List<Module> res = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(getProject()).getModules()) {
            if (module.getExtension(DModuleExtension.class) != null) {
                res.add(module);
            }
        }
        return res;
    }

    @Override
    public RunProfileState getState(Executor executor, ExecutionEnvironment env) throws ExecutionException {
        return new DlangRunDubState(env, this);
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        Module module = getConfigurationModule().getModule();
        if (module == null) {
            throw new ExecutionException(DLocalize.dubRunNoModule().get());
        }
        Path executable = DubUtil.findTargetExecutable(module);
        if (executable == null) {
            throw new ExecutionException(DLocalize.dubRunNoTargetExecutable(module.getName()).get());
        }
        return createDebugTarget(executable, DubUtil.findPackageDirectory(module));
    }

    @Override
    public boolean isBuildProjectOnEmptyModuleList() {
        return true;
    }

    private static int readInt(Element element, String name, int defaultValue) {
        String value = element.getAttributeValue(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean readBoolean(Element element, String name) {
        return Boolean.parseBoolean(element.getAttributeValue(name));
    }

    public int getGeneralDubOptions() {
        return getOptions().getGeneralDubOptions();
    }

    public void setGeneralDubOptions(int generalDubOptions) {
        getOptions().setGeneralDubOptions(generalDubOptions);
    }

    public boolean isCbRdmd() {
        return getOptions().isCbRdmd();
    }

    public void setCbRdmd(boolean cbRdmd) {
        getOptions().setCbRdmd(cbRdmd);
    }

    public @Nullable String getTfBuild() {
        return getOptions().getTfBuild();
    }

    public void setTfBuild(@Nullable String tfBuild) {
        getOptions().setTfBuild(tfBuild);
    }

    public @Nullable String getTfCompiler() {
        return getOptions().getTfCompiler();
    }

    public void setTfCompiler(@Nullable String tfCompiler) {
        getOptions().setTfCompiler(tfCompiler);
    }

    public @Nullable String getTfConfig() {
        return getOptions().getTfConfig();
    }

    public void setTfConfig(@Nullable String tfConfig) {
        getOptions().setTfConfig(tfConfig);
    }

    public @Nullable String getTfArch() {
        return getOptions().getTfArch();
    }

    public void setTfArch(@Nullable String tfArch) {
        getOptions().setTfArch(tfArch);
    }

    public @Nullable String getTfDebug() {
        return getOptions().getTfDebug();
    }

    public void setTfDebug(@Nullable String tfDebug) {
        getOptions().setTfDebug(tfDebug);
    }

    public int getBuildMode() {
        return getOptions().getBuildMode();
    }

    public void setBuildMode(int buildMode) {
        getOptions().setBuildMode(buildMode);
    }

    public boolean isCbForce() {
        return getOptions().isCbForce();
    }

    public void setCbForce(boolean cbForce) {
        getOptions().setCbForce(cbForce);
    }

    public boolean isCbNoDeps() {
        return getOptions().isCbNoDeps();
    }

    public void setCbNoDeps(boolean cbNoDeps) {
        getOptions().setCbNoDeps(cbNoDeps);
    }

    public boolean isCbForceRemove() {
        return getOptions().isCbForceRemove();
    }

    public void setCbForceRemove(boolean cbForceRemove) {
        getOptions().setCbForceRemove(cbForceRemove);
    }

    public boolean isCbCombined() {
        return getOptions().isCbCombined();
    }

    public void setCbCombined(boolean cbCombined) {
        getOptions().setCbCombined(cbCombined);
    }

    public boolean isCbParallel() {
        return getOptions().isCbParallel();
    }

    public void setCbParallel(boolean cbParallel) {
        getOptions().setCbParallel(cbParallel);
    }

    public boolean isCbTempBuild() {
        return getOptions().isCbTempBuild();
    }

    public void setCbTempBuild(boolean cbTempBuild) {
        getOptions().setCbTempBuild(cbTempBuild);
    }

    public @Nullable String getTfMainFile() {
        return getOptions().getTfMainFile();
    }

    public void setTfMainFile(@Nullable String tfMainFile) {
        getOptions().setTfMainFile(tfMainFile);
    }

    public boolean isCbCoverage() {
        return getOptions().isCbCoverage();
    }

    public void setCbCoverage(boolean cbCoverage) {
        getOptions().setCbCoverage(cbCoverage);
    }

    public boolean isQuiet() {
        return getOptions().isQuiet();
    }

    public void setQuiet(boolean quiet) {
        getOptions().setQuiet(quiet);
    }

    public boolean isVerbose() {
        return getOptions().isVerbose();
    }

    public void setVerbose(boolean verbose) {
        getOptions().setVerbose(verbose);
    }

    public @Nullable String getAdditionalParams() {
        return getOptions().getAdditionalParams();
    }

    public void setAdditionalParams(@Nullable String additionalParams) {
        getOptions().setAdditionalParams(additionalParams);
    }
}

package io.github.intellij.dub.run;

import consulo.d.dub.DubUtil;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RunProfileWithCompileBeforeLaunchOption;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.run.NativeProgramRunConfiguration;
import consulo.nativeDev.debugger.run.NativeProgramSettings;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Port of upstream {@code DlangRunDubConfiguration.kt}. The working directory, the environment and the debugger come
 * from nativeDev's {@link NativeProgramSettings}; the program arguments are passed after {@code --} to {@code dub run}.
 * Debugging launches the executable the root package builds to, as the last DUB import found it.
 */
public class DlangRunDubConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
    implements NativeProgramRunConfiguration, RunProfileWithCompileBeforeLaunchOption {

    //General tab
    private int myGeneralDubOptions = 1;

    //Build tab
    private boolean myCbRdmd = false;
    private @Nullable String myTfBuild = null;
    private @Nullable String myTfCompiler = null;

    // Run tab
    private @Nullable String myTfConfig = null;
    private @Nullable String myTfArch = null;
    private @Nullable String myTfDebug = null;
    private int myBuildMode = 0;
    private boolean myCbForce = false;
    private boolean myCbNoDeps = false;
    private boolean myCbForceRemove = false;
    private boolean myCbCombined = false;
    private boolean myCbParallel = false;
    private boolean myCbTempBuild = false;

    // Test tab
    private @Nullable String myTfMainFile = null;
    private boolean myCbCoverage = false;

    //DUB properties
    private boolean myQuiet = false;
    private boolean myVerbose = false;
    private @Nullable String myAdditionalParams = null;

    private final NativeProgramSettings myProgramSettings = new NativeProgramSettings();

    public DlangRunDubConfiguration(String name, Project project, ConfigurationFactory factory) {
        super(name, new RunConfigurationModule(project), factory);
        Collection<Module> modules = getValidModules();
        if (!modules.isEmpty()) {
            //Get first valid module and use it
            setModule(modules.iterator().next());
        }
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
    public NativeProgramSettings getProgramSettings() {
        return myProgramSettings;
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DlangRunDubConfigurationEditor(getProject());
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
        return myProgramSettings.createDebugTarget(executable, DubUtil.findPackageDirectory(module));
    }

    @Override
    public boolean isBuildProjectOnEmptyModuleList() {
        return true;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        readModule(element);
        myGeneralDubOptions = readInt(element, "generalDubOptions", 1);
        myCbRdmd = readBoolean(element, "cbRdmd");
        myTfBuild = element.getAttributeValue("tfBuild");
        myTfCompiler = element.getAttributeValue("tfCompiler");
        myTfConfig = element.getAttributeValue("tfConfig");
        myTfArch = element.getAttributeValue("tfArch");
        myTfDebug = element.getAttributeValue("tfDebug");
        myBuildMode = readInt(element, "buildMode", 0);
        myCbForce = readBoolean(element, "cbForce");
        myCbNoDeps = readBoolean(element, "cbNoDeps");
        myCbForceRemove = readBoolean(element, "cbForceRemove");
        myCbCombined = readBoolean(element, "cbCombined");
        myCbParallel = readBoolean(element, "cbParallel");
        myCbTempBuild = readBoolean(element, "cbTempBuild");
        myTfMainFile = element.getAttributeValue("tfMainFile");
        myCbCoverage = readBoolean(element, "cbCoverage");
        myQuiet = readBoolean(element, "quiet");
        myVerbose = readBoolean(element, "verbose");
        myAdditionalParams = element.getAttributeValue("additionalParams");
        myProgramSettings.readExternal(element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeModule(element);
        element.setAttribute("generalDubOptions", String.valueOf(myGeneralDubOptions));
        element.setAttribute("cbRdmd", String.valueOf(myCbRdmd));
        writeString(element, "tfBuild", myTfBuild);
        writeString(element, "tfCompiler", myTfCompiler);
        writeString(element, "tfConfig", myTfConfig);
        writeString(element, "tfArch", myTfArch);
        writeString(element, "tfDebug", myTfDebug);
        element.setAttribute("buildMode", String.valueOf(myBuildMode));
        element.setAttribute("cbForce", String.valueOf(myCbForce));
        element.setAttribute("cbNoDeps", String.valueOf(myCbNoDeps));
        element.setAttribute("cbForceRemove", String.valueOf(myCbForceRemove));
        element.setAttribute("cbCombined", String.valueOf(myCbCombined));
        element.setAttribute("cbParallel", String.valueOf(myCbParallel));
        element.setAttribute("cbTempBuild", String.valueOf(myCbTempBuild));
        writeString(element, "tfMainFile", myTfMainFile);
        element.setAttribute("cbCoverage", String.valueOf(myCbCoverage));
        element.setAttribute("quiet", String.valueOf(myQuiet));
        element.setAttribute("verbose", String.valueOf(myVerbose));
        writeString(element, "additionalParams", myAdditionalParams);
        myProgramSettings.writeExternal(element);
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

    private static void writeString(Element element, String name, @Nullable String value) {
        if (value != null) {
            element.setAttribute(name, value);
        }
    }

    public int getGeneralDubOptions() {
        return myGeneralDubOptions;
    }

    public void setGeneralDubOptions(int generalDubOptions) {
        myGeneralDubOptions = generalDubOptions;
    }

    public boolean isCbRdmd() {
        return myCbRdmd;
    }

    public void setCbRdmd(boolean cbRdmd) {
        myCbRdmd = cbRdmd;
    }

    public @Nullable String getTfBuild() {
        return myTfBuild;
    }

    public void setTfBuild(@Nullable String tfBuild) {
        myTfBuild = tfBuild;
    }

    public @Nullable String getTfCompiler() {
        return myTfCompiler;
    }

    public void setTfCompiler(@Nullable String tfCompiler) {
        myTfCompiler = tfCompiler;
    }

    public @Nullable String getTfConfig() {
        return myTfConfig;
    }

    public void setTfConfig(@Nullable String tfConfig) {
        myTfConfig = tfConfig;
    }

    public @Nullable String getTfArch() {
        return myTfArch;
    }

    public void setTfArch(@Nullable String tfArch) {
        myTfArch = tfArch;
    }

    public @Nullable String getTfDebug() {
        return myTfDebug;
    }

    public void setTfDebug(@Nullable String tfDebug) {
        myTfDebug = tfDebug;
    }

    public int getBuildMode() {
        return myBuildMode;
    }

    public void setBuildMode(int buildMode) {
        myBuildMode = buildMode;
    }

    public boolean isCbForce() {
        return myCbForce;
    }

    public void setCbForce(boolean cbForce) {
        myCbForce = cbForce;
    }

    public boolean isCbNoDeps() {
        return myCbNoDeps;
    }

    public void setCbNoDeps(boolean cbNoDeps) {
        myCbNoDeps = cbNoDeps;
    }

    public boolean isCbForceRemove() {
        return myCbForceRemove;
    }

    public void setCbForceRemove(boolean cbForceRemove) {
        myCbForceRemove = cbForceRemove;
    }

    public boolean isCbCombined() {
        return myCbCombined;
    }

    public void setCbCombined(boolean cbCombined) {
        myCbCombined = cbCombined;
    }

    public boolean isCbParallel() {
        return myCbParallel;
    }

    public void setCbParallel(boolean cbParallel) {
        myCbParallel = cbParallel;
    }

    public boolean isCbTempBuild() {
        return myCbTempBuild;
    }

    public void setCbTempBuild(boolean cbTempBuild) {
        myCbTempBuild = cbTempBuild;
    }

    public @Nullable String getTfMainFile() {
        return myTfMainFile;
    }

    public void setTfMainFile(@Nullable String tfMainFile) {
        myTfMainFile = tfMainFile;
    }

    public boolean isCbCoverage() {
        return myCbCoverage;
    }

    public void setCbCoverage(boolean cbCoverage) {
        myCbCoverage = cbCoverage;
    }

    public boolean isQuiet() {
        return myQuiet;
    }

    public void setQuiet(boolean quiet) {
        myQuiet = quiet;
    }

    public boolean isVerbose() {
        return myVerbose;
    }

    public void setVerbose(boolean verbose) {
        myVerbose = verbose;
    }

    public @Nullable String getAdditionalParams() {
        return myAdditionalParams;
    }

    public void setAdditionalParams(@Nullable String additionalParams) {
        myAdditionalParams = additionalParams;
    }
}

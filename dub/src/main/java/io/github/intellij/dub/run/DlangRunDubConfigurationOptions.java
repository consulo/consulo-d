package io.github.intellij.dub.run;

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class DlangRunDubConfigurationOptions extends RunConfigurationOptions {
    private int myGeneralDubOptions = 1;
    private boolean myCbRdmd;
    private @Nullable String myTfBuild;
    private @Nullable String myTfCompiler;
    private @Nullable String myTfConfig;
    private @Nullable String myTfArch;
    private @Nullable String myTfDebug;
    private int myBuildMode;
    private boolean myCbForce;
    private boolean myCbNoDeps;
    private boolean myCbForceRemove;
    private boolean myCbCombined;
    private boolean myCbParallel;
    private boolean myCbTempBuild;
    private @Nullable String myTfMainFile;
    private boolean myCbCoverage;
    private boolean myQuiet;
    private boolean myVerbose;
    private @Nullable String myAdditionalParams;

    @Attribute("generalDubOptions")
    public int getGeneralDubOptions() {
        return myGeneralDubOptions;
    }

    public void setGeneralDubOptions(int generalDubOptions) {
        myGeneralDubOptions = generalDubOptions;
    }

    @Attribute("cbRdmd")
    public boolean isCbRdmd() {
        return myCbRdmd;
    }

    public void setCbRdmd(boolean cbRdmd) {
        myCbRdmd = cbRdmd;
    }

    @Attribute("tfBuild")
    public @Nullable String getTfBuild() {
        return myTfBuild;
    }

    public void setTfBuild(@Nullable String tfBuild) {
        myTfBuild = StringUtil.nullize(tfBuild, true);
    }

    @Attribute("tfCompiler")
    public @Nullable String getTfCompiler() {
        return myTfCompiler;
    }

    public void setTfCompiler(@Nullable String tfCompiler) {
        myTfCompiler = StringUtil.nullize(tfCompiler, true);
    }

    @Attribute("tfConfig")
    public @Nullable String getTfConfig() {
        return myTfConfig;
    }

    public void setTfConfig(@Nullable String tfConfig) {
        myTfConfig = StringUtil.nullize(tfConfig, true);
    }

    @Attribute("tfArch")
    public @Nullable String getTfArch() {
        return myTfArch;
    }

    public void setTfArch(@Nullable String tfArch) {
        myTfArch = StringUtil.nullize(tfArch, true);
    }

    @Attribute("tfDebug")
    public @Nullable String getTfDebug() {
        return myTfDebug;
    }

    public void setTfDebug(@Nullable String tfDebug) {
        myTfDebug = StringUtil.nullize(tfDebug, true);
    }

    @Attribute("buildMode")
    public int getBuildMode() {
        return myBuildMode;
    }

    public void setBuildMode(int buildMode) {
        myBuildMode = buildMode;
    }

    @Attribute("cbForce")
    public boolean isCbForce() {
        return myCbForce;
    }

    public void setCbForce(boolean cbForce) {
        myCbForce = cbForce;
    }

    @Attribute("cbNoDeps")
    public boolean isCbNoDeps() {
        return myCbNoDeps;
    }

    public void setCbNoDeps(boolean cbNoDeps) {
        myCbNoDeps = cbNoDeps;
    }

    @Attribute("cbForceRemove")
    public boolean isCbForceRemove() {
        return myCbForceRemove;
    }

    public void setCbForceRemove(boolean cbForceRemove) {
        myCbForceRemove = cbForceRemove;
    }

    @Attribute("cbCombined")
    public boolean isCbCombined() {
        return myCbCombined;
    }

    public void setCbCombined(boolean cbCombined) {
        myCbCombined = cbCombined;
    }

    @Attribute("cbParallel")
    public boolean isCbParallel() {
        return myCbParallel;
    }

    public void setCbParallel(boolean cbParallel) {
        myCbParallel = cbParallel;
    }

    @Attribute("cbTempBuild")
    public boolean isCbTempBuild() {
        return myCbTempBuild;
    }

    public void setCbTempBuild(boolean cbTempBuild) {
        myCbTempBuild = cbTempBuild;
    }

    @Attribute("tfMainFile")
    public @Nullable String getTfMainFile() {
        return myTfMainFile;
    }

    public void setTfMainFile(@Nullable String tfMainFile) {
        myTfMainFile = StringUtil.nullize(tfMainFile, true);
    }

    @Attribute("cbCoverage")
    public boolean isCbCoverage() {
        return myCbCoverage;
    }

    public void setCbCoverage(boolean cbCoverage) {
        myCbCoverage = cbCoverage;
    }

    @Attribute("quiet")
    public boolean isQuiet() {
        return myQuiet;
    }

    public void setQuiet(boolean quiet) {
        myQuiet = quiet;
    }

    @Attribute("verbose")
    public boolean isVerbose() {
        return myVerbose;
    }

    public void setVerbose(boolean verbose) {
        myVerbose = verbose;
    }

    @Attribute("additionalParams")
    public @Nullable String getAdditionalParams() {
        return myAdditionalParams;
    }

    public void setAdditionalParams(@Nullable String additionalParams) {
        myAdditionalParams = StringUtil.nullize(additionalParams, true);
    }
}

package io.github.intellij.dlanguage.run;

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class DlangRunDmdConfigurationOptions extends RunConfigurationOptions {
    private boolean myBetterC;
    private boolean myRelease;
    private boolean myDebug;
    private boolean myUnitTest;
    private boolean myLink;
    private boolean myCoverageAnalysis;
    private boolean myAllowDeprecated;
    private boolean myIgnorePragmas;
    private boolean myFunctionInlining;
    private boolean myLibrary;
    private boolean myNoArrayBoundsCheck;
    private boolean myNoFloatingPointReferences;
    private boolean myOptimize;
    private boolean myEnforcePropertySyntax;
    private boolean myQuiet;
    private boolean myVerbose;
    private boolean myListThreadLocalStorage;
    private boolean myWarnings;
    private boolean myInfoWarnings;
    private @Nullable String myDefaultLibrary;
    private @Nullable String myImportsPath;
    private @Nullable String myStringImportsPath;
    private @Nullable String myLinkerArgs;
    private boolean myGenerateDocs;
    private @Nullable String myDocsFilename;
    private @Nullable String myDocsPath;
    private @Nullable String myModuleDepsFilename;
    private boolean myGenerateHeader;
    private @Nullable String myHeaderDir;
    private @Nullable String myHeaderFilename;
    private boolean myGenerateMap;
    private boolean myNoObjectFiles;
    private boolean myNoStripPaths;
    private boolean myGenerateJson;
    private @Nullable String myJsonFilename;
    private boolean myAddSymbolicDebugInfo;
    private boolean myGenerateStandardStackFrame;
    private @Nullable String mySymbolicLibrary;
    private boolean myProfile;

    @Attribute("betterC")
    public boolean isBetterC() {
        return myBetterC;
    }

    public void setBetterC(boolean betterC) {
        myBetterC = betterC;
    }

    @Attribute("release")
    public boolean isRelease() {
        return myRelease;
    }

    public void setRelease(boolean release) {
        myRelease = release;
    }

    @Attribute("debug")
    public boolean isDebug() {
        return myDebug;
    }

    public void setDebug(boolean debug) {
        myDebug = debug;
    }

    @Attribute("unitTest")
    public boolean isUnitTest() {
        return myUnitTest;
    }

    public void setUnitTest(boolean unitTest) {
        myUnitTest = unitTest;
    }

    @Attribute("link")
    public boolean isLink() {
        return myLink;
    }

    public void setLink(boolean link) {
        myLink = link;
    }

    @Attribute("coverageAnalysis")
    public boolean isCoverageAnalysis() {
        return myCoverageAnalysis;
    }

    public void setCoverageAnalysis(boolean coverageAnalysis) {
        myCoverageAnalysis = coverageAnalysis;
    }

    @Attribute("allowDeprecated")
    public boolean isAllowDeprecated() {
        return myAllowDeprecated;
    }

    public void setAllowDeprecated(boolean allowDeprecated) {
        myAllowDeprecated = allowDeprecated;
    }

    @Attribute("ignorePragmas")
    public boolean isIgnorePragmas() {
        return myIgnorePragmas;
    }

    public void setIgnorePragmas(boolean ignorePragmas) {
        myIgnorePragmas = ignorePragmas;
    }

    @Attribute("functionInlining")
    public boolean isFunctionInlining() {
        return myFunctionInlining;
    }

    public void setFunctionInlining(boolean functionInlining) {
        myFunctionInlining = functionInlining;
    }

    @Attribute("library")
    public boolean isLibrary() {
        return myLibrary;
    }

    public void setLibrary(boolean library) {
        myLibrary = library;
    }

    @Attribute("noArrayBoundsCheck")
    public boolean isNoArrayBoundsCheck() {
        return myNoArrayBoundsCheck;
    }

    public void setNoArrayBoundsCheck(boolean noArrayBoundsCheck) {
        myNoArrayBoundsCheck = noArrayBoundsCheck;
    }

    @Attribute("noFloatingPointReferences")
    public boolean isNoFloatingPointReferences() {
        return myNoFloatingPointReferences;
    }

    public void setNoFloatingPointReferences(boolean noFloatingPointReferences) {
        myNoFloatingPointReferences = noFloatingPointReferences;
    }

    @Attribute("optimize")
    public boolean isOptimize() {
        return myOptimize;
    }

    public void setOptimize(boolean optimize) {
        myOptimize = optimize;
    }

    @Attribute("enforcePropertySyntax")
    public boolean isEnforcePropertySyntax() {
        return myEnforcePropertySyntax;
    }

    public void setEnforcePropertySyntax(boolean enforcePropertySyntax) {
        myEnforcePropertySyntax = enforcePropertySyntax;
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

    @Attribute("listThreadLocalStorage")
    public boolean isListThreadLocalStorage() {
        return myListThreadLocalStorage;
    }

    public void setListThreadLocalStorage(boolean listThreadLocalStorage) {
        myListThreadLocalStorage = listThreadLocalStorage;
    }

    @Attribute("warnings")
    public boolean isWarnings() {
        return myWarnings;
    }

    public void setWarnings(boolean warnings) {
        myWarnings = warnings;
    }

    @Attribute("infoWarnings")
    public boolean isInfoWarnings() {
        return myInfoWarnings;
    }

    public void setInfoWarnings(boolean infoWarnings) {
        myInfoWarnings = infoWarnings;
    }

    @Attribute("defaultLibrary")
    public @Nullable String getDefaultLibrary() {
        return myDefaultLibrary;
    }

    public void setDefaultLibrary(@Nullable String defaultLibrary) {
        myDefaultLibrary = StringUtil.nullize(defaultLibrary, true);
    }

    @Attribute("importsPath")
    public @Nullable String getImportsPath() {
        return myImportsPath;
    }

    public void setImportsPath(@Nullable String importsPath) {
        myImportsPath = StringUtil.nullize(importsPath, true);
    }

    @Attribute("stringImportsPath")
    public @Nullable String getStringImportsPath() {
        return myStringImportsPath;
    }

    public void setStringImportsPath(@Nullable String stringImportsPath) {
        myStringImportsPath = StringUtil.nullize(stringImportsPath, true);
    }

    @Attribute("linkerArgs")
    public @Nullable String getLinkerArgs() {
        return myLinkerArgs;
    }

    public void setLinkerArgs(@Nullable String linkerArgs) {
        myLinkerArgs = StringUtil.nullize(linkerArgs, true);
    }

    @Attribute("generateDocs")
    public boolean isGenerateDocs() {
        return myGenerateDocs;
    }

    public void setGenerateDocs(boolean generateDocs) {
        myGenerateDocs = generateDocs;
    }

    @Attribute("docsFilename")
    public @Nullable String getDocsFilename() {
        return myDocsFilename;
    }

    public void setDocsFilename(@Nullable String docsFilename) {
        myDocsFilename = StringUtil.nullize(docsFilename, true);
    }

    @Attribute("docsPath")
    public @Nullable String getDocsPath() {
        return myDocsPath;
    }

    public void setDocsPath(@Nullable String docsPath) {
        myDocsPath = StringUtil.nullize(docsPath, true);
    }

    @Attribute("moduleDepsFilename")
    public @Nullable String getModuleDepsFilename() {
        return myModuleDepsFilename;
    }

    public void setModuleDepsFilename(@Nullable String moduleDepsFilename) {
        myModuleDepsFilename = StringUtil.nullize(moduleDepsFilename, true);
    }

    @Attribute("generateHeader")
    public boolean isGenerateHeader() {
        return myGenerateHeader;
    }

    public void setGenerateHeader(boolean generateHeader) {
        myGenerateHeader = generateHeader;
    }

    @Attribute("headerDir")
    public @Nullable String getHeaderDir() {
        return myHeaderDir;
    }

    public void setHeaderDir(@Nullable String headerDir) {
        myHeaderDir = StringUtil.nullize(headerDir, true);
    }

    @Attribute("headerFilename")
    public @Nullable String getHeaderFilename() {
        return myHeaderFilename;
    }

    public void setHeaderFilename(@Nullable String headerFilename) {
        myHeaderFilename = StringUtil.nullize(headerFilename, true);
    }

    @Attribute("generateMap")
    public boolean isGenerateMap() {
        return myGenerateMap;
    }

    public void setGenerateMap(boolean generateMap) {
        myGenerateMap = generateMap;
    }

    @Attribute("noObjectFiles")
    public boolean isNoObjectFiles() {
        return myNoObjectFiles;
    }

    public void setNoObjectFiles(boolean noObjectFiles) {
        myNoObjectFiles = noObjectFiles;
    }

    @Attribute("noStripPaths")
    public boolean isNoStripPaths() {
        return myNoStripPaths;
    }

    public void setNoStripPaths(boolean noStripPaths) {
        myNoStripPaths = noStripPaths;
    }

    @Attribute("generateJson")
    public boolean isGenerateJson() {
        return myGenerateJson;
    }

    public void setGenerateJson(boolean generateJson) {
        myGenerateJson = generateJson;
    }

    @Attribute("jsonFilename")
    public @Nullable String getJsonFilename() {
        return myJsonFilename;
    }

    public void setJsonFilename(@Nullable String jsonFilename) {
        myJsonFilename = StringUtil.nullize(jsonFilename, true);
    }

    @Attribute("addSymbolicDebugInfo")
    public boolean isAddSymbolicDebugInfo() {
        return myAddSymbolicDebugInfo;
    }

    public void setAddSymbolicDebugInfo(boolean addSymbolicDebugInfo) {
        myAddSymbolicDebugInfo = addSymbolicDebugInfo;
    }

    @Attribute("generateStandardStackFrame")
    public boolean isGenerateStandardStackFrame() {
        return myGenerateStandardStackFrame;
    }

    public void setGenerateStandardStackFrame(boolean generateStandardStackFrame) {
        myGenerateStandardStackFrame = generateStandardStackFrame;
    }

    @Attribute("symbolicLibrary")
    public @Nullable String getSymbolicLibrary() {
        return mySymbolicLibrary;
    }

    public void setSymbolicLibrary(@Nullable String symbolicLibrary) {
        mySymbolicLibrary = StringUtil.nullize(symbolicLibrary, true);
    }

    @Attribute("profile")
    public boolean isProfile() {
        return myProfile;
    }

    public void setProfile(boolean profile) {
        myProfile = profile;
    }
}

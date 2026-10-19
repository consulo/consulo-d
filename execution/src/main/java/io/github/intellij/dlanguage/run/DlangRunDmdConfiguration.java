package io.github.intellij.dlanguage.run;


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

/**
 * Consulo: Run compiles, as upstream; Debug launches the program this configuration builds (the {@code -of} output)
 * under the nativeDev debugger, with the program arguments, working directory and environment of
 * {@link NativeProgramSettings}. The options are written as upstream's {@code XmlSerializer} wrote them
 * ({@code <option name=".." value=".."/>}). IntelliJ's {@code isBuildBeforeLaunchAddedByDefault} and
 * {@code isExcludeCompileBeforeLaunchOption} have no Consulo counterpart.
 */
public class DlangRunDmdConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
    implements NativeProgramRunConfiguration, RunProfileWithCompileBeforeLaunchOption {

    private static final String OPTION = "option";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    //Compiler settings properties
    private boolean betterC;
    private boolean release;
    private boolean debug;
    private boolean unitTest;
    private boolean link;
    private boolean coverageAnalysis;
    private boolean allowDeprecated;
    private boolean ignorePragmas;
    private boolean functionInlining;
    private boolean library;
    private boolean noArrayBoundsCheck;
    private boolean noFloatingPointReferences;
    private boolean optimize;
    private boolean enforcePropertySyntax;
    private boolean quiet;
    private boolean verbose;
    private boolean listThreadLocalStorage;
    private boolean warnings;
    private boolean infoWarnings;
    private @Nullable String defaultLibrary;
    private @Nullable String importsPath;
    private @Nullable String stringImportsPath;
    private @Nullable String linkerArgs;

    //Output settings properties
    private boolean generateDocs;
    private @Nullable String docsFilename;
    private @Nullable String docsPath;
    private @Nullable String moduleDepsFilename;
    private boolean generateHeader;
    private @Nullable String headerDir;
    private @Nullable String headerFilename;
    private boolean generateMap;
    private boolean noObjectFiles;
    private boolean noStripPaths;
    private boolean generateJson;
    private @Nullable String jsonFilename;

    //Debug settings properties
    private boolean addSymbolicDebugInfo;
    private boolean generateStandardStackFrame;
    private @Nullable String symbolicLibrary;
    private boolean profile;

    private final NativeProgramSettings programSettings = new NativeProgramSettings();


    public DlangRunDmdConfiguration(final String name, final Project project, final ConfigurationFactory factory) {
        super(name, new RunConfigurationModule(project), factory);

        final Collection<Module> modules = this.getValidModules();
        if (!modules.isEmpty()) {
            //Get first valid module and use it
            this.setModule(modules.iterator().next());
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
        return new DlangRunDmdConfigurationEditor(getProject());
    }

    @Override
    public RunProfileState getState(final Executor executor, final ExecutionEnvironment env) throws ExecutionException {
        return new DlangRunDmdState(env, this);
    }

    @Override
    public NativeProgramSettings getProgramSettings() {
        return programSettings;
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        Module module = getConfigurationModule().getModule();
        if (module == null) {
            throw new ExecutionException(DLocalize.runNoModuleSelected().get());
        }
        Path executable = Path.of(DlangDmdConfigToArgsConverter.getOutputFilePath(module, this));
        String basePath = getProject().getBasePath();
        return programSettings.createDebugTarget(executable, basePath == null ? null : Path.of(basePath));
    }

    @Override
    public void writeExternal(final Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeOption(element, "betterC", String.valueOf(betterC));
        writeOption(element, "release", String.valueOf(release));
        writeOption(element, "debug", String.valueOf(debug));
        writeOption(element, "unitTest", String.valueOf(unitTest));
        writeOption(element, "link", String.valueOf(link));
        writeOption(element, "coverageAnalysis", String.valueOf(coverageAnalysis));
        writeOption(element, "allowDeprecated", String.valueOf(allowDeprecated));
        writeOption(element, "ignorePragmas", String.valueOf(ignorePragmas));
        writeOption(element, "functionInlining", String.valueOf(functionInlining));
        writeOption(element, "library", String.valueOf(library));
        writeOption(element, "noArrayBoundsCheck", String.valueOf(noArrayBoundsCheck));
        writeOption(element, "noFloatingPointReferences", String.valueOf(noFloatingPointReferences));
        writeOption(element, "optimize", String.valueOf(optimize));
        writeOption(element, "enforcePropertySyntax", String.valueOf(enforcePropertySyntax));
        writeOption(element, "quiet", String.valueOf(quiet));
        writeOption(element, "verbose", String.valueOf(verbose));
        writeOption(element, "listThreadLocalStorage", String.valueOf(listThreadLocalStorage));
        writeOption(element, "warnings", String.valueOf(warnings));
        writeOption(element, "infoWarnings", String.valueOf(infoWarnings));
        writeOption(element, "defaultLibrary", defaultLibrary);
        writeOption(element, "importsPath", importsPath);
        writeOption(element, "stringImportsPath", stringImportsPath);
        writeOption(element, "linkerArgs", linkerArgs);
        writeOption(element, "generateDocs", String.valueOf(generateDocs));
        writeOption(element, "docsFilename", docsFilename);
        writeOption(element, "docsPath", docsPath);
        writeOption(element, "moduleDepsFilename", moduleDepsFilename);
        writeOption(element, "generateHeader", String.valueOf(generateHeader));
        writeOption(element, "headerDir", headerDir);
        writeOption(element, "headerFilename", headerFilename);
        writeOption(element, "generateMap", String.valueOf(generateMap));
        writeOption(element, "noObjectFiles", String.valueOf(noObjectFiles));
        writeOption(element, "noStripPaths", String.valueOf(noStripPaths));
        writeOption(element, "generateJson", String.valueOf(generateJson));
        writeOption(element, "jsonFilename", jsonFilename);
        writeOption(element, "addSymbolicDebugInfo", String.valueOf(addSymbolicDebugInfo));
        writeOption(element, "generateStandardStackFrame", String.valueOf(generateStandardStackFrame));
        writeOption(element, "symbolicLibrary", symbolicLibrary);
        writeOption(element, "profile", String.valueOf(profile));
        programSettings.writeExternal(element);
    }

    @Override
    public void readExternal(final Element element) throws InvalidDataException {
        super.readExternal(element);
        betterC = readBoolean(element, "betterC");
        release = readBoolean(element, "release");
        debug = readBoolean(element, "debug");
        unitTest = readBoolean(element, "unitTest");
        link = readBoolean(element, "link");
        coverageAnalysis = readBoolean(element, "coverageAnalysis");
        allowDeprecated = readBoolean(element, "allowDeprecated");
        ignorePragmas = readBoolean(element, "ignorePragmas");
        functionInlining = readBoolean(element, "functionInlining");
        library = readBoolean(element, "library");
        noArrayBoundsCheck = readBoolean(element, "noArrayBoundsCheck");
        noFloatingPointReferences = readBoolean(element, "noFloatingPointReferences");
        optimize = readBoolean(element, "optimize");
        enforcePropertySyntax = readBoolean(element, "enforcePropertySyntax");
        quiet = readBoolean(element, "quiet");
        verbose = readBoolean(element, "verbose");
        listThreadLocalStorage = readBoolean(element, "listThreadLocalStorage");
        warnings = readBoolean(element, "warnings");
        infoWarnings = readBoolean(element, "infoWarnings");
        defaultLibrary = readOption(element, "defaultLibrary");
        importsPath = readOption(element, "importsPath");
        stringImportsPath = readOption(element, "stringImportsPath");
        linkerArgs = readOption(element, "linkerArgs");
        generateDocs = readBoolean(element, "generateDocs");
        docsFilename = readOption(element, "docsFilename");
        docsPath = readOption(element, "docsPath");
        moduleDepsFilename = readOption(element, "moduleDepsFilename");
        generateHeader = readBoolean(element, "generateHeader");
        headerDir = readOption(element, "headerDir");
        headerFilename = readOption(element, "headerFilename");
        generateMap = readBoolean(element, "generateMap");
        noObjectFiles = readBoolean(element, "noObjectFiles");
        noStripPaths = readBoolean(element, "noStripPaths");
        generateJson = readBoolean(element, "generateJson");
        jsonFilename = readOption(element, "jsonFilename");
        addSymbolicDebugInfo = readBoolean(element, "addSymbolicDebugInfo");
        generateStandardStackFrame = readBoolean(element, "generateStandardStackFrame");
        symbolicLibrary = readOption(element, "symbolicLibrary");
        profile = readBoolean(element, "profile");
        programSettings.readExternal(element);
    }

    private static void writeOption(Element element, String name, @Nullable String value) {
        if (value != null) {
            element.addContent(new Element(OPTION).setAttribute(NAME, name).setAttribute(VALUE, value));
        }
    }

    private static @Nullable String readOption(Element element, String name) {
        for (Element option : element.getChildren(OPTION)) {
            if (name.equals(option.getAttributeValue(NAME))) {
                return option.getAttributeValue(VALUE);
            }
        }
        return null;
    }

    private static boolean readBoolean(Element element, String name) {
        return Boolean.parseBoolean(readOption(element, name));
    }

    public boolean isBetterC() {
        return this.betterC;
    }

    public void setBetterC(boolean betterC) {
        this.betterC = betterC;
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(final boolean release) {
        this.release = release;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public boolean isUnitTest() {
        return unitTest;
    }

    public void setUnitTest(final boolean unitTest) {
        this.unitTest = unitTest;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(final boolean link) {
        this.link = link;
    }

    public boolean isCoverageAnalysis() {
        return coverageAnalysis;
    }

    public void setCoverageAnalysis(final boolean coverageAnalysis) {
        this.coverageAnalysis = coverageAnalysis;
    }

    public boolean isAllowDeprecated() {
        return allowDeprecated;
    }

    public void setAllowDeprecated(final boolean allowDeprecated) {
        this.allowDeprecated = allowDeprecated;
    }

    public boolean isIgnorePragmas() {
        return ignorePragmas;
    }

    public void setIgnorePragmas(final boolean ignorePragmas) {
        this.ignorePragmas = ignorePragmas;
    }

    public boolean isFunctionInlining() {
        return functionInlining;
    }

    public void setFunctionInlining(final boolean functionInlining) {
        this.functionInlining = functionInlining;
    }

    public boolean isLibrary() {
        return library;
    }

    public void setLibrary(final boolean library) {
        this.library = library;
    }

    public boolean isNoArrayBoundsCheck() {
        return noArrayBoundsCheck;
    }

    public void setNoArrayBoundsCheck(final boolean noArrayBoundsCheck) {
        this.noArrayBoundsCheck = noArrayBoundsCheck;
    }

    public boolean isNoFloatingPointReferences() {
        return noFloatingPointReferences;
    }

    public void setNoFloatingPointReferences(final boolean noFloatingPointReferences) {
        this.noFloatingPointReferences = noFloatingPointReferences;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(final boolean optimize) {
        this.optimize = optimize;
    }

    public boolean isEnforcePropertySyntax() {
        return enforcePropertySyntax;
    }

    public void setEnforcePropertySyntax(final boolean enforcePropertySyntax) {
        this.enforcePropertySyntax = enforcePropertySyntax;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(final boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isListThreadLocalStorage() {
        return listThreadLocalStorage;
    }

    public void setListThreadLocalStorage(final boolean listThreadLocalStorage) {
        this.listThreadLocalStorage = listThreadLocalStorage;
    }

    public boolean isWarnings() {
        return warnings;
    }

    public void setWarnings(final boolean warnings) {
        this.warnings = warnings;
    }

    public boolean isInfoWarnings() {
        return infoWarnings;
    }

    public void setInfoWarnings(final boolean infoWarnings) {
        this.infoWarnings = infoWarnings;
    }

    public @Nullable String getDefaultLibrary() {
        return defaultLibrary;
    }

    public void setDefaultLibrary(final @Nullable String defaultLibrary) {
        this.defaultLibrary = defaultLibrary;
    }

    public @Nullable String getImportsPath() {
        return importsPath;
    }

    public void setImportsPath(final @Nullable String importsPath) {
        this.importsPath = importsPath;
    }

    public @Nullable String getStringImportsPath() {
        return stringImportsPath;
    }

    public void setStringImportsPath(final @Nullable String stringImportsPath) {
        this.stringImportsPath = stringImportsPath;
    }

    public @Nullable String getLinkerArgs() {
        return linkerArgs;
    }

    public void setLinkerArgs(final @Nullable String linkerArgs) {
        this.linkerArgs = linkerArgs;
    }

    public @Nullable String getJsonFilename() {
        return jsonFilename;
    }

    public void setJsonFilename(final @Nullable String jsonFilename) {
        this.jsonFilename = jsonFilename;
    }

    public boolean isGenerateJson() {
        return generateJson;
    }

    public void setGenerateJson(final boolean generateJson) {
        this.generateJson = generateJson;
    }

    public boolean isNoStripPaths() {
        return noStripPaths;
    }

    public void setNoStripPaths(final boolean noStripPaths) {
        this.noStripPaths = noStripPaths;
    }

    public boolean isNoObjectFiles() {
        return noObjectFiles;
    }

    public void setNoObjectFiles(final boolean noObjectFiles) {
        this.noObjectFiles = noObjectFiles;
    }

    public boolean isGenerateMap() {
        return generateMap;
    }

    public void setGenerateMap(final boolean generateMap) {
        this.generateMap = generateMap;
    }

    public @Nullable String getHeaderFilename() {
        return headerFilename;
    }

    public void setHeaderFilename(final @Nullable String headerFilename) {
        this.headerFilename = headerFilename;
    }

    public @Nullable String getHeaderDir() {
        return headerDir;
    }

    public void setHeaderDir(final @Nullable String headerDir) {
        this.headerDir = headerDir;
    }

    public boolean isGenerateHeader() {
        return generateHeader;
    }

    public void setGenerateHeader(final boolean generateHeader) {
        this.generateHeader = generateHeader;
    }

    public @Nullable String getModuleDepsFilename() {
        return moduleDepsFilename;
    }

    public void setModuleDepsFilename(final @Nullable String moduleDepsFilename) {
        this.moduleDepsFilename = moduleDepsFilename;
    }

    public @Nullable String getDocsPath() {
        return docsPath;
    }

    public void setDocsPath(final @Nullable String docsPath) {
        this.docsPath = docsPath;
    }

    public @Nullable String getDocsFilename() {
        return docsFilename;
    }

    public void setDocsFilename(final @Nullable String docsFilename) {
        this.docsFilename = docsFilename;
    }

    public boolean isGenerateDocs() {
        return generateDocs;
    }

    public void setGenerateDocs(final boolean generateDocs) {
        this.generateDocs = generateDocs;
    }

    public boolean isAddSymbolicDebugInfo() {
        return addSymbolicDebugInfo;
    }

    public void setAddSymbolicDebugInfo(final boolean addSymbolicDebugInfo) {
        this.addSymbolicDebugInfo = addSymbolicDebugInfo;
    }

    public boolean isGenerateStandardStackFrame() {
        return generateStandardStackFrame;
    }

    public void setGenerateStandardStackFrame(final boolean generateStandardStackFrame) {
        this.generateStandardStackFrame = generateStandardStackFrame;
    }

    public @Nullable String getSymbolicLibrary() {
        return symbolicLibrary;
    }

    public void setSymbolicLibrary(final @Nullable String symbolicLibrary) {
        this.symbolicLibrary = symbolicLibrary;
    }

    public boolean isProfile() {
        return profile;
    }

    public void setProfile(final boolean profile) {
        this.profile = profile;
    }

    @Override
    public boolean isBuildProjectOnEmptyModuleList() {
        return true;
    }
}

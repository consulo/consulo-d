package io.github.intellij.dlanguage.run;


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

/**
 * Consulo: Run compiles, as upstream; Debug launches the program this configuration builds (the {@code -of} output)
 * under the nativeDev debugger, with the program arguments, working directory and environment of the shared
 * program fragments. Old configurations stored the options as upstream's {@code XmlSerializer} wrote them
 * ({@code <option name=".." value=".."/>}). IntelliJ's {@code isBuildBeforeLaunchAddedByDefault} and
 * {@code isExcludeCompileBeforeLaunchOption} have no Consulo counterpart.
 */
public class DlangRunDmdConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
    implements NativeProgramRunConfiguration, RunProfileWithCompileBeforeLaunchOption {

    private static final String OPTION = "option";
    private static final String NAME = "name";
    private static final String VALUE = "value";

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
    protected Class<DlangRunDmdConfigurationOptions> getOptionsClass() {
        return DlangRunDmdConfigurationOptions.class;
    }

    @Override
    public DlangRunDmdConfigurationOptions getOptions() {
        return (DlangRunDmdConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(final LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        final Element element = converter.getElement();
        final DlangRunDmdConfigurationOptions options = new DlangRunDmdConfigurationOptions();
        options.setBetterC(readBoolean(element, "betterC"));
        options.setRelease(readBoolean(element, "release"));
        options.setDebug(readBoolean(element, "debug"));
        options.setUnitTest(readBoolean(element, "unitTest"));
        options.setLink(readBoolean(element, "link"));
        options.setCoverageAnalysis(readBoolean(element, "coverageAnalysis"));
        options.setAllowDeprecated(readBoolean(element, "allowDeprecated"));
        options.setIgnorePragmas(readBoolean(element, "ignorePragmas"));
        options.setFunctionInlining(readBoolean(element, "functionInlining"));
        options.setLibrary(readBoolean(element, "library"));
        options.setNoArrayBoundsCheck(readBoolean(element, "noArrayBoundsCheck"));
        options.setNoFloatingPointReferences(readBoolean(element, "noFloatingPointReferences"));
        options.setOptimize(readBoolean(element, "optimize"));
        options.setEnforcePropertySyntax(readBoolean(element, "enforcePropertySyntax"));
        options.setQuiet(readBoolean(element, "quiet"));
        options.setVerbose(readBoolean(element, "verbose"));
        options.setListThreadLocalStorage(readBoolean(element, "listThreadLocalStorage"));
        options.setWarnings(readBoolean(element, "warnings"));
        options.setInfoWarnings(readBoolean(element, "infoWarnings"));
        options.setDefaultLibrary(readOption(element, "defaultLibrary"));
        options.setImportsPath(readOption(element, "importsPath"));
        options.setStringImportsPath(readOption(element, "stringImportsPath"));
        options.setLinkerArgs(readOption(element, "linkerArgs"));
        options.setGenerateDocs(readBoolean(element, "generateDocs"));
        options.setDocsFilename(readOption(element, "docsFilename"));
        options.setDocsPath(readOption(element, "docsPath"));
        options.setModuleDepsFilename(readOption(element, "moduleDepsFilename"));
        options.setGenerateHeader(readBoolean(element, "generateHeader"));
        options.setHeaderDir(readOption(element, "headerDir"));
        options.setHeaderFilename(readOption(element, "headerFilename"));
        options.setGenerateMap(readBoolean(element, "generateMap"));
        options.setNoObjectFiles(readBoolean(element, "noObjectFiles"));
        options.setNoStripPaths(readBoolean(element, "noStripPaths"));
        options.setGenerateJson(readBoolean(element, "generateJson"));
        options.setJsonFilename(readOption(element, "jsonFilename"));
        options.setAddSymbolicDebugInfo(readBoolean(element, "addSymbolicDebugInfo"));
        options.setGenerateStandardStackFrame(readBoolean(element, "generateStandardStackFrame"));
        options.setSymbolicLibrary(readOption(element, "symbolicLibrary"));
        options.setProfile(readBoolean(element, "profile"));
        converter.setOptions(options);

        NativeProgramRunConfiguration.convertLegacyProgramState(converter);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        return DlangRunDmdFragmentFactory.create();
    }

    @Override
    public RunProfileState getState(final Executor executor, final ExecutionEnvironment env) throws ExecutionException {
        return new DlangRunDmdState(env, this);
    }

    @Override
    public NativeDebugTarget createDebugTarget(ExecutionEnvironment environment) throws ExecutionException {
        Module module = getConfigurationModule().getModule();
        if (module == null) {
            throw new ExecutionException(DLocalize.runNoModuleSelected().get());
        }
        Path executable = Path.of(DlangDmdConfigToArgsConverter.getOutputFilePath(module, this));
        String basePath = getProject().getBasePath();
        return createDebugTarget(executable, basePath == null ? null : Path.of(basePath));
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
        return getOptions().isBetterC();
    }

    public void setBetterC(boolean betterC) {
        getOptions().setBetterC(betterC);
    }

    public boolean isRelease() {
        return getOptions().isRelease();
    }

    public void setRelease(final boolean release) {
        getOptions().setRelease(release);
    }

    public boolean isDebug() {
        return getOptions().isDebug();
    }

    public void setDebug(final boolean debug) {
        getOptions().setDebug(debug);
    }

    public boolean isUnitTest() {
        return getOptions().isUnitTest();
    }

    public void setUnitTest(final boolean unitTest) {
        getOptions().setUnitTest(unitTest);
    }

    public boolean isLink() {
        return getOptions().isLink();
    }

    public void setLink(final boolean link) {
        getOptions().setLink(link);
    }

    public boolean isCoverageAnalysis() {
        return getOptions().isCoverageAnalysis();
    }

    public void setCoverageAnalysis(final boolean coverageAnalysis) {
        getOptions().setCoverageAnalysis(coverageAnalysis);
    }

    public boolean isAllowDeprecated() {
        return getOptions().isAllowDeprecated();
    }

    public void setAllowDeprecated(final boolean allowDeprecated) {
        getOptions().setAllowDeprecated(allowDeprecated);
    }

    public boolean isIgnorePragmas() {
        return getOptions().isIgnorePragmas();
    }

    public void setIgnorePragmas(final boolean ignorePragmas) {
        getOptions().setIgnorePragmas(ignorePragmas);
    }

    public boolean isFunctionInlining() {
        return getOptions().isFunctionInlining();
    }

    public void setFunctionInlining(final boolean functionInlining) {
        getOptions().setFunctionInlining(functionInlining);
    }

    public boolean isLibrary() {
        return getOptions().isLibrary();
    }

    public void setLibrary(final boolean library) {
        getOptions().setLibrary(library);
    }

    public boolean isNoArrayBoundsCheck() {
        return getOptions().isNoArrayBoundsCheck();
    }

    public void setNoArrayBoundsCheck(final boolean noArrayBoundsCheck) {
        getOptions().setNoArrayBoundsCheck(noArrayBoundsCheck);
    }

    public boolean isNoFloatingPointReferences() {
        return getOptions().isNoFloatingPointReferences();
    }

    public void setNoFloatingPointReferences(final boolean noFloatingPointReferences) {
        getOptions().setNoFloatingPointReferences(noFloatingPointReferences);
    }

    public boolean isOptimize() {
        return getOptions().isOptimize();
    }

    public void setOptimize(final boolean optimize) {
        getOptions().setOptimize(optimize);
    }

    public boolean isEnforcePropertySyntax() {
        return getOptions().isEnforcePropertySyntax();
    }

    public void setEnforcePropertySyntax(final boolean enforcePropertySyntax) {
        getOptions().setEnforcePropertySyntax(enforcePropertySyntax);
    }

    public boolean isQuiet() {
        return getOptions().isQuiet();
    }

    public void setQuiet(final boolean quiet) {
        getOptions().setQuiet(quiet);
    }

    public boolean isVerbose() {
        return getOptions().isVerbose();
    }

    public void setVerbose(final boolean verbose) {
        getOptions().setVerbose(verbose);
    }

    public boolean isListThreadLocalStorage() {
        return getOptions().isListThreadLocalStorage();
    }

    public void setListThreadLocalStorage(final boolean listThreadLocalStorage) {
        getOptions().setListThreadLocalStorage(listThreadLocalStorage);
    }

    public boolean isWarnings() {
        return getOptions().isWarnings();
    }

    public void setWarnings(final boolean warnings) {
        getOptions().setWarnings(warnings);
    }

    public boolean isInfoWarnings() {
        return getOptions().isInfoWarnings();
    }

    public void setInfoWarnings(final boolean infoWarnings) {
        getOptions().setInfoWarnings(infoWarnings);
    }

    public @Nullable String getDefaultLibrary() {
        return getOptions().getDefaultLibrary();
    }

    public void setDefaultLibrary(final @Nullable String defaultLibrary) {
        getOptions().setDefaultLibrary(defaultLibrary);
    }

    public @Nullable String getImportsPath() {
        return getOptions().getImportsPath();
    }

    public void setImportsPath(final @Nullable String importsPath) {
        getOptions().setImportsPath(importsPath);
    }

    public @Nullable String getStringImportsPath() {
        return getOptions().getStringImportsPath();
    }

    public void setStringImportsPath(final @Nullable String stringImportsPath) {
        getOptions().setStringImportsPath(stringImportsPath);
    }

    public @Nullable String getLinkerArgs() {
        return getOptions().getLinkerArgs();
    }

    public void setLinkerArgs(final @Nullable String linkerArgs) {
        getOptions().setLinkerArgs(linkerArgs);
    }

    public @Nullable String getJsonFilename() {
        return getOptions().getJsonFilename();
    }

    public void setJsonFilename(final @Nullable String jsonFilename) {
        getOptions().setJsonFilename(jsonFilename);
    }

    public boolean isGenerateJson() {
        return getOptions().isGenerateJson();
    }

    public void setGenerateJson(final boolean generateJson) {
        getOptions().setGenerateJson(generateJson);
    }

    public boolean isNoStripPaths() {
        return getOptions().isNoStripPaths();
    }

    public void setNoStripPaths(final boolean noStripPaths) {
        getOptions().setNoStripPaths(noStripPaths);
    }

    public boolean isNoObjectFiles() {
        return getOptions().isNoObjectFiles();
    }

    public void setNoObjectFiles(final boolean noObjectFiles) {
        getOptions().setNoObjectFiles(noObjectFiles);
    }

    public boolean isGenerateMap() {
        return getOptions().isGenerateMap();
    }

    public void setGenerateMap(final boolean generateMap) {
        getOptions().setGenerateMap(generateMap);
    }

    public @Nullable String getHeaderFilename() {
        return getOptions().getHeaderFilename();
    }

    public void setHeaderFilename(final @Nullable String headerFilename) {
        getOptions().setHeaderFilename(headerFilename);
    }

    public @Nullable String getHeaderDir() {
        return getOptions().getHeaderDir();
    }

    public void setHeaderDir(final @Nullable String headerDir) {
        getOptions().setHeaderDir(headerDir);
    }

    public boolean isGenerateHeader() {
        return getOptions().isGenerateHeader();
    }

    public void setGenerateHeader(final boolean generateHeader) {
        getOptions().setGenerateHeader(generateHeader);
    }

    public @Nullable String getModuleDepsFilename() {
        return getOptions().getModuleDepsFilename();
    }

    public void setModuleDepsFilename(final @Nullable String moduleDepsFilename) {
        getOptions().setModuleDepsFilename(moduleDepsFilename);
    }

    public @Nullable String getDocsPath() {
        return getOptions().getDocsPath();
    }

    public void setDocsPath(final @Nullable String docsPath) {
        getOptions().setDocsPath(docsPath);
    }

    public @Nullable String getDocsFilename() {
        return getOptions().getDocsFilename();
    }

    public void setDocsFilename(final @Nullable String docsFilename) {
        getOptions().setDocsFilename(docsFilename);
    }

    public boolean isGenerateDocs() {
        return getOptions().isGenerateDocs();
    }

    public void setGenerateDocs(final boolean generateDocs) {
        getOptions().setGenerateDocs(generateDocs);
    }

    public boolean isAddSymbolicDebugInfo() {
        return getOptions().isAddSymbolicDebugInfo();
    }

    public void setAddSymbolicDebugInfo(final boolean addSymbolicDebugInfo) {
        getOptions().setAddSymbolicDebugInfo(addSymbolicDebugInfo);
    }

    public boolean isGenerateStandardStackFrame() {
        return getOptions().isGenerateStandardStackFrame();
    }

    public void setGenerateStandardStackFrame(final boolean generateStandardStackFrame) {
        getOptions().setGenerateStandardStackFrame(generateStandardStackFrame);
    }

    public @Nullable String getSymbolicLibrary() {
        return getOptions().getSymbolicLibrary();
    }

    public void setSymbolicLibrary(final @Nullable String symbolicLibrary) {
        getOptions().setSymbolicLibrary(symbolicLibrary);
    }

    public boolean isProfile() {
        return getOptions().isProfile();
    }

    public void setProfile(final boolean profile) {
        getOptions().setProfile(profile);
    }

    @Override
    public boolean isBuildProjectOnEmptyModuleList() {
        return true;
    }
}

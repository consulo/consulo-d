package io.github.intellij.dlanguage.run;

import consulo.d.execution.fragment.DGroupRunConfigurationFragment;
import consulo.d.execution.fragment.DTagRunConfigurationFragment;
import consulo.d.execution.fragment.DTextRunConfigurationFragment;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The fragments of {@link DlangRunDmdConfiguration}: the compilation, output and debug options of DMD in sections, and
 * the DMD arguments they produce.
 */
public final class DlangRunDmdFragmentFactory {
    private static final String COMPILATION = "dmd.compilation";
    private static final String OUTPUT = "dmd.output";
    private static final String DEBUG = "dmd.debug";

    private DlangRunDmdFragmentFactory() {
    }

    public static List<RunConfigurationFragment<?>> create() {
        List<RunConfigurationFragment<?>> fragments = new ArrayList<>();

        fragments.add(new DGroupRunConfigurationFragment<DlangRunDmdConfiguration>(COMPILATION, DLocalize.dmdRunConfigTabCompilation()));
        fragments.add(text(COMPILATION, "dmd.defaultLibrary", DLocalize.runConfigurationFragmentDmdDefaultLibrary(),
            DLocalize.dmdRunConfigDefaultLibraryLabel(),
            DlangRunDmdConfiguration::getDefaultLibrary, DlangRunDmdConfiguration::setDefaultLibrary));
        fragments.add(text(COMPILATION, "dmd.imports", DLocalize.runConfigurationFragmentDmdImports(),
            DLocalize.dmdRunConfigImportsLabel(),
            DlangRunDmdConfiguration::getImportsPath, DlangRunDmdConfiguration::setImportsPath)
            .withField(DlangRunDmdFragmentFactory::createFolderField));
        fragments.add(text(COMPILATION, "dmd.stringImports", DLocalize.runConfigurationFragmentDmdStringImports(),
            DLocalize.dmdRunConfigStringImportsLabel(),
            DlangRunDmdConfiguration::getStringImportsPath, DlangRunDmdConfiguration::setStringImportsPath)
            .withField(DlangRunDmdFragmentFactory::createFolderField));
        fragments.add(text(COMPILATION, "dmd.linkerArgs", DLocalize.runConfigurationFragmentDmdLinkerArguments(),
            DLocalize.dmdRunConfigLinkerArgumentsLabel(),
            DlangRunDmdConfiguration::getLinkerArgs, DlangRunDmdConfiguration::setLinkerArgs)
            .withField(DlangRunDmdFragmentFactory::createArgumentsField));
        fragments.add(tag(COMPILATION, "dmd.release", DLocalize.dmdRunConfigRelease(),
            DlangRunDmdConfiguration::isRelease, DlangRunDmdConfiguration::setRelease));
        fragments.add(tag(COMPILATION, "dmd.debugBuild", DLocalize.dmdRunConfigDebug(),
            DlangRunDmdConfiguration::isDebug, DlangRunDmdConfiguration::setDebug));
        fragments.add(tag(COMPILATION, "dmd.unitTest", DLocalize.dmdRunConfigUnitTest(),
            DlangRunDmdConfiguration::isUnitTest, DlangRunDmdConfiguration::setUnitTest));
        fragments.add(tag(COMPILATION, "dmd.link", DLocalize.dmdRunConfigLink(),
            DlangRunDmdConfiguration::isLink, DlangRunDmdConfiguration::setLink));
        fragments.add(tag(COMPILATION, "dmd.coverageAnalysis", DLocalize.dmdRunConfigCoverageAnalysis(),
            DlangRunDmdConfiguration::isCoverageAnalysis, DlangRunDmdConfiguration::setCoverageAnalysis));
        fragments.add(tag(COMPILATION, "dmd.allowDeprecated", DLocalize.dmdRunConfigAllowDeprecated(),
            DlangRunDmdConfiguration::isAllowDeprecated, DlangRunDmdConfiguration::setAllowDeprecated));
        fragments.add(tag(COMPILATION, "dmd.ignorePragmas", DLocalize.dmdRunConfigIgnorePragmas(),
            DlangRunDmdConfiguration::isIgnorePragmas, DlangRunDmdConfiguration::setIgnorePragmas));
        fragments.add(tag(COMPILATION, "dmd.functionInlining", DLocalize.dmdRunConfigFunctionInlining(),
            DlangRunDmdConfiguration::isFunctionInlining, DlangRunDmdConfiguration::setFunctionInlining));
        fragments.add(tag(COMPILATION, "dmd.library", DLocalize.dmdRunConfigLibrary(),
            DlangRunDmdConfiguration::isLibrary, DlangRunDmdConfiguration::setLibrary));
        fragments.add(tag(COMPILATION, "dmd.noArrayBoundsCheck", DLocalize.dmdRunConfigNoArrayBoundsCheck(),
            DlangRunDmdConfiguration::isNoArrayBoundsCheck, DlangRunDmdConfiguration::setNoArrayBoundsCheck));
        fragments.add(tag(COMPILATION, "dmd.noFloatingPointReferences", DLocalize.dmdRunConfigNoFloatingPointReferences(),
            DlangRunDmdConfiguration::isNoFloatingPointReferences, DlangRunDmdConfiguration::setNoFloatingPointReferences));
        fragments.add(tag(COMPILATION, "dmd.optimize", DLocalize.dmdRunConfigOptimize(),
            DlangRunDmdConfiguration::isOptimize, DlangRunDmdConfiguration::setOptimize));
        fragments.add(tag(COMPILATION, "dmd.enforcePropertySyntax", DLocalize.dmdRunConfigEnforcePropertySyntax(),
            DlangRunDmdConfiguration::isEnforcePropertySyntax, DlangRunDmdConfiguration::setEnforcePropertySyntax));
        fragments.add(tag(COMPILATION, "dmd.quiet", DLocalize.dmdRunConfigQuiet(),
            DlangRunDmdConfiguration::isQuiet, DlangRunDmdConfiguration::setQuiet));
        fragments.add(tag(COMPILATION, "dmd.verbose", DLocalize.dmdRunConfigVerbose(),
            DlangRunDmdConfiguration::isVerbose, DlangRunDmdConfiguration::setVerbose));
        fragments.add(tag(COMPILATION, "dmd.listThreadLocalStorage", DLocalize.dmdRunConfigListThreadLocalStorage(),
            DlangRunDmdConfiguration::isListThreadLocalStorage, DlangRunDmdConfiguration::setListThreadLocalStorage));
        fragments.add(tag(COMPILATION, "dmd.warnings", DLocalize.dmdRunConfigWarnings(),
            DlangRunDmdConfiguration::isWarnings, DlangRunDmdConfiguration::setWarnings));
        fragments.add(tag(COMPILATION, "dmd.infoWarnings", DLocalize.dmdRunConfigInfoWarnings(),
            DlangRunDmdConfiguration::isInfoWarnings, DlangRunDmdConfiguration::setInfoWarnings));

        fragments.add(new DGroupRunConfigurationFragment<DlangRunDmdConfiguration>(OUTPUT, DLocalize.dmdRunConfigTabOutput()));
        fragments.add(tag(OUTPUT, "dmd.generateDocs", DLocalize.dmdRunConfigGenerateDocumentation(),
            DlangRunDmdConfiguration::isGenerateDocs, DlangRunDmdConfiguration::setGenerateDocs));
        fragments.add(text(OUTPUT, "dmd.docsPath", DLocalize.runConfigurationFragmentDmdDocumentationFolder(),
            DLocalize.dmdRunConfigDocumentationFolderLabel(),
            DlangRunDmdConfiguration::getDocsPath, DlangRunDmdConfiguration::setDocsPath)
            .withField(DlangRunDmdFragmentFactory::createFolderField));
        fragments.add(text(OUTPUT, "dmd.docsFilename", DLocalize.runConfigurationFragmentDmdDocumentationFilename(),
            DLocalize.dmdRunConfigDocumentationFilenameLabel(),
            DlangRunDmdConfiguration::getDocsFilename, DlangRunDmdConfiguration::setDocsFilename));
        fragments.add(text(OUTPUT, "dmd.moduleDepsFilename", DLocalize.runConfigurationFragmentDmdModuleDependencies(),
            DLocalize.dmdRunConfigModuleDependenciesLabel(),
            DlangRunDmdConfiguration::getModuleDepsFilename, DlangRunDmdConfiguration::setModuleDepsFilename));
        fragments.add(tag(OUTPUT, "dmd.generateHeader", DLocalize.dmdRunConfigGenerateHeader(),
            DlangRunDmdConfiguration::isGenerateHeader, DlangRunDmdConfiguration::setGenerateHeader));
        fragments.add(text(OUTPUT, "dmd.headerDir", DLocalize.runConfigurationFragmentDmdHeaderDirectory(),
            DLocalize.dmdRunConfigHeaderDirectoryLabel(),
            DlangRunDmdConfiguration::getHeaderDir, DlangRunDmdConfiguration::setHeaderDir)
            .withField(DlangRunDmdFragmentFactory::createFolderField));
        fragments.add(text(OUTPUT, "dmd.headerFilename", DLocalize.runConfigurationFragmentDmdHeaderFilename(),
            DLocalize.dmdRunConfigHeaderFilenameLabel(),
            DlangRunDmdConfiguration::getHeaderFilename, DlangRunDmdConfiguration::setHeaderFilename));
        fragments.add(tag(OUTPUT, "dmd.noObjectFiles", DLocalize.dmdRunConfigNoObjectFiles(),
            DlangRunDmdConfiguration::isNoObjectFiles, DlangRunDmdConfiguration::setNoObjectFiles));
        fragments.add(tag(OUTPUT, "dmd.noStripPaths", DLocalize.dmdRunConfigNoStripPaths(),
            DlangRunDmdConfiguration::isNoStripPaths, DlangRunDmdConfiguration::setNoStripPaths));
        fragments.add(tag(OUTPUT, "dmd.generateJson", DLocalize.dmdRunConfigGenerateJson(),
            DlangRunDmdConfiguration::isGenerateJson, DlangRunDmdConfiguration::setGenerateJson));
        fragments.add(text(OUTPUT, "dmd.jsonFilename", DLocalize.runConfigurationFragmentDmdJsonFilename(),
            DLocalize.dmdRunConfigJsonFilenameLabel(),
            DlangRunDmdConfiguration::getJsonFilename, DlangRunDmdConfiguration::setJsonFilename));
        fragments.add(tag(OUTPUT, "dmd.generateMap", DLocalize.dmdRunConfigGenerateMap(),
            DlangRunDmdConfiguration::isGenerateMap, DlangRunDmdConfiguration::setGenerateMap));

        fragments.add(new DGroupRunConfigurationFragment<DlangRunDmdConfiguration>(DEBUG, DLocalize.dmdRunConfigTabDebug()));
        fragments.add(tag(DEBUG, "dmd.addSymbolicDebugInfo", DLocalize.dmdRunConfigAddSymbolicDebugInfo(),
            DlangRunDmdConfiguration::isAddSymbolicDebugInfo, DlangRunDmdConfiguration::setAddSymbolicDebugInfo));
        fragments.add(tag(DEBUG, "dmd.generateStandardStackFrame", DLocalize.dmdRunConfigGenerateStandardStackFrame(),
            DlangRunDmdConfiguration::isGenerateStandardStackFrame, DlangRunDmdConfiguration::setGenerateStandardStackFrame));
        fragments.add(text(DEBUG, "dmd.symbolicLibrary", DLocalize.runConfigurationFragmentDmdSymbolicLibrary(),
            DLocalize.dmdRunConfigSymbolicLibraryLabel(),
            DlangRunDmdConfiguration::getSymbolicLibrary, DlangRunDmdConfiguration::setSymbolicLibrary));
        fragments.add(tag(DEBUG, "dmd.profile", DLocalize.dmdRunConfigProfile(),
            DlangRunDmdConfiguration::isProfile, DlangRunDmdConfiguration::setProfile));

        fragments.add(new DlangDmdArgumentsFragment());
        return fragments;
    }

    private static DTextRunConfigurationFragment<DlangRunDmdConfiguration> text(String parentId,
                                                                               String id,
                                                                               LocalizeValue name,
                                                                               LocalizeValue label,
                                                                               Function<DlangRunDmdConfiguration, @Nullable String> getter,
                                                                               BiConsumer<DlangRunDmdConfiguration, @Nullable String> setter) {
        return new DTextRunConfigurationFragment<>(id, name, label, getter, setter).withParentId(parentId);
    }

    private static DTagRunConfigurationFragment<DlangRunDmdConfiguration> tag(String parentId,
                                                                             String id,
                                                                             LocalizeValue name,
                                                                             Predicate<DlangRunDmdConfiguration> getter,
                                                                             BiConsumer<DlangRunDmdConfiguration, Boolean> setter) {
        return new DTagRunConfigurationFragment<>(id, name, getter, setter).withParentId(parentId);
    }

    @RequiredUIAccess
    private static ValueComponent<String> createFolderField(Project project) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withShowFileSystemRoots(true)
            .withTitle(DLocalize.dmdRunConfigSelectimportfolderTitle())
            .withDescription(DLocalize.dmdRunConfigSelectimportfolderDescription())
            .withHideIgnored(false);
        return FileChooserTextBoxBuilder.create(project)
            .fileChooserDescriptor(descriptor)
            .dialogTitle(DLocalize.dmdRunConfigSelectimportfolderTitle())
            .dialogDescription(DLocalize.dmdRunConfigSelectimportfolderDescription())
            .build()
            .getComponent();
    }

    @RequiredUIAccess
    private static ValueComponent<String> createArgumentsField(Project project) {
        return TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            "",
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );
    }
}

package io.github.intellij.dlanguage.run;

import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.run.NativeProgramParametersLayout;
import consulo.platform.Platform;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ui.CheckBox;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.Hyperlink;
import consulo.ui.Label;
import consulo.ui.TextArea;
import consulo.ui.TextBox;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.layout.TabbedLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.run.exception.NoSourcesException;
import org.jspecify.annotations.Nullable;

/**
 * Consulo: built with Consulo UI instead of upstream's Swing form, with a fifth tab for nativeDev's program
 * parameters and debugger (see {@link DlangRunDmdConfiguration}). Consulo UI components do not report changes to the
 * editor by themselves, so the form fires the editor state change that refreshes the "Arguments" tab.
 */
public class DlangRunDmdConfigurationEditor extends SettingsEditor<DlangRunDmdConfiguration> {

    private static final Logger LOG = Logger.getInstance(DlangRunDmdConfigurationEditor.class);

    private final Project project;
    private @Nullable Form form;

    public DlangRunDmdConfigurationEditor(final Project project) {
        this.project = project;
    }

    /**
     * Update editor UI with data of DLangRunDmdConfiguration.
     * All components must be changed according to "config" data.
     */
    @RequiredUIAccess
    @Override
    protected void resetEditorFrom(final DlangRunDmdConfiguration config) {
        final Form form = this.form;
        if (form != null) {
            form.resetCompilerTabForm(config);
            form.resetOutputTabForm(config);
            form.resetDebugTabForm(config);
            form.programLayout.reset(config);
            form.fillArguments(config);
        }
    }

    /**
     * Save state of editor UI to DLangRunDmdConfiguration instance.
     */
    @RequiredUIAccess
    @Override
    protected void applyEditorTo(final DlangRunDmdConfiguration config) {
        final Form form = this.form;
        if (form != null) {
            form.applyCompilerTabForm(config);
            form.applyOutputTabForm(config);
            form.applyDebugTabForm(config);
            form.programLayout.apply(config);
        }
    }

    @RequiredUIAccess
    @Override
    protected Component createUIComponent() {
        final Form form = new Form();
        this.form = form;

        /* Each time settings are changed "Arguments" tab must be updated.
         * Add listener here. */
        final DlangRunDmdConfigurationType configurationType = DlangRunDmdConfigurationType.getInstance();
        final ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];

        addSettingsEditorListener(editor -> {
            try {
                final Module module = form.getSelectedModule();
                if (module != null) {
                    final DlangRunDmdConfiguration config = (DlangRunDmdConfiguration) factory.createTemplateConfiguration(module.getProject());
                    applyEditorTo(config); //Save current editor state to "config"
                    form.fillArguments(config); //Convert "config" to DMD arguments to display on "Arguments" tab.
                }
            } catch (final Exception e) {
                //pass
            }
        });

        return form.mainPanel;
    }

    private class Form {
        private final TabbedLayout mainPanel = TabbedLayout.create();

        //"Compile" tab
        private final ComboBox<String> comboModules;
        private final CheckBox cbRelease = CheckBox.create(DLocalize.dmdRunConfigRelease());
        private final CheckBox cbDebug = CheckBox.create(DLocalize.dmdRunConfigDebug());
        private final CheckBox cbUnitTest = CheckBox.create(DLocalize.dmdRunConfigUnitTest());
        private final CheckBox cbLink = CheckBox.create(DLocalize.dmdRunConfigLink());
        private final CheckBox cbCoverageAnalysis = CheckBox.create(DLocalize.dmdRunConfigCoverageAnalysis());
        private final CheckBox cbAllowDeprecated = CheckBox.create(DLocalize.dmdRunConfigAllowDeprecated());
        private final CheckBox cbIgnorePragmas = CheckBox.create(DLocalize.dmdRunConfigIgnorePragmas());
        private final CheckBox cbFunctionInlining = CheckBox.create(DLocalize.dmdRunConfigFunctionInlining());
        private final CheckBox cbLibrary = CheckBox.create(DLocalize.dmdRunConfigLibrary());
        private final CheckBox cbNoArrayBoundsCheck = CheckBox.create(DLocalize.dmdRunConfigNoArrayBoundsCheck());
        private final CheckBox cbNoFloatingPointReferences = CheckBox.create(DLocalize.dmdRunConfigNoFloatingPointReferences());
        private final CheckBox cbOptimize = CheckBox.create(DLocalize.dmdRunConfigOptimize());
        private final CheckBox cbEnforcePropertySyntax = CheckBox.create(DLocalize.dmdRunConfigEnforcePropertySyntax());
        private final CheckBox cbQuiet = CheckBox.create(DLocalize.dmdRunConfigQuiet());
        private final CheckBox cbVerbose = CheckBox.create(DLocalize.dmdRunConfigVerbose());
        private final CheckBox cbListThreadLocalStorage = CheckBox.create(DLocalize.dmdRunConfigListThreadLocalStorage());
        private final CheckBox cbWarnings = CheckBox.create(DLocalize.dmdRunConfigWarnings());
        private final CheckBox cbInfoWarnings = CheckBox.create(DLocalize.dmdRunConfigInfoWarnings());
        private final TextBox textDefaultLibrary = TextBox.create();
        private final FileChooserTextBoxBuilder.Controller pathImports;
        private final FileChooserTextBoxBuilder.Controller pathStringImports;
        private final TextBoxWithExpandAction linkerArgs = TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            "",
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );

        //"Output" tab
        private final CheckBox cbGenerateDocumentation = CheckBox.create(DLocalize.dmdRunConfigGenerateDocumentation());
        private final TextBox filenameDocumentation = TextBox.create();
        private final FileChooserTextBoxBuilder.Controller pathDocumentation;
        private final TextBox filenameModuleDependencies = TextBox.create();
        private final CheckBox cbGenerateHeader = CheckBox.create(DLocalize.dmdRunConfigGenerateHeader());
        private final FileChooserTextBoxBuilder.Controller pathHeaderDir;
        private final TextBox filenameHeader = TextBox.create();
        private final CheckBox cbGenerateMap = CheckBox.create(DLocalize.dmdRunConfigGenerateMap());
        private final CheckBox cbNoObjectFiles = CheckBox.create(DLocalize.dmdRunConfigNoObjectFiles());
        private final CheckBox cbNoStripPaths = CheckBox.create(DLocalize.dmdRunConfigNoStripPaths());
        private final CheckBox cbGenerateJson = CheckBox.create(DLocalize.dmdRunConfigGenerateJson());
        private final TextBox filenameJson = TextBox.create();

        //"Debug" tab
        private final CheckBox cbAddSymbolicDebugInfo = CheckBox.create(DLocalize.dmdRunConfigAddSymbolicDebugInfo());
        private final CheckBox cbGenerateStandardStackFrame = CheckBox.create(DLocalize.dmdRunConfigGenerateStandardStackFrame());
        private final TextBox textSymbolicLibrary = TextBox.create();
        private final CheckBox cbProfile = CheckBox.create(DLocalize.dmdRunConfigProfile());

        //"Arguments" tab
        private final Hyperlink linkDmdDoc;
        private final TextArea textArgsPane = TextArea.create();

        // the nativeDev program parameters and debugger
        private final NativeProgramParametersLayout<DlangRunDmdConfiguration> programLayout;

        @RequiredUIAccess
        Form() {
            final ComboBox.Builder<String> modules = ComboBox.builder();
            for (final Module module : ModuleManager.getInstance(project).getModules()) {
                if (module.getExtension(DModuleExtension.class) != null) {
                    modules.add(module.getName(), LocalizeValue.of(module.getName()));
                }
            }
            comboModules = modules.build();

            final FileChooserDescriptor fcd = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withShowFileSystemRoots(true)
                .withTitle(DLocalize.dmdRunConfigSelectimportfolderTitle())
                .withDescription(DLocalize.dmdRunConfigSelectimportfolderDescription())
                .withHideIgnored(false);

            pathImports = createFolderField(fcd);

            //XXX: fix title and description
            pathStringImports = createFolderField(fcd);

            //XXX: fix title and description
            pathDocumentation = createFolderField(fcd);

            //XXX: fix title and description
            pathHeaderDir = createFolderField(fcd);

            filenameDocumentation.setEnabled(false);
            pathDocumentation.getComponent().setEnabled(false);
            pathHeaderDir.getComponent().setEnabled(false);
            filenameHeader.setEnabled(false);
            filenameJson.setEnabled(false);

            cbGenerateDocumentation.addValueListener(e -> {
                final boolean enabled = cbGenerateDocumentation.getValueOrError();
                filenameDocumentation.setEnabled(enabled);
                pathDocumentation.getComponent().setEnabled(enabled);
            });

            cbGenerateHeader.addValueListener(e -> {
                final boolean enabled = cbGenerateHeader.getValueOrError();
                pathHeaderDir.getComponent().setEnabled(enabled);
                filenameHeader.setEnabled(enabled);
            });

            cbGenerateJson.addValueListener(e -> filenameJson.setEnabled(cbGenerateJson.getValueOrError()));

            linkDmdDoc = setDocLink();

            textArgsPane.setEditable(false);

            programLayout = new NativeProgramParametersLayout<>(project);
            programLayout.build();

            final FormBuilder compiler = FormBuilder.create();
            compiler.addLabeled(DLocalize.runConfigModuleLabel(), comboModules);
            compiler.addLabeled(DLocalize.dmdRunConfigDefaultLibraryLabel(), textDefaultLibrary);
            compiler.addLabeled(DLocalize.dmdRunConfigImportsLabel(), pathImports.getComponent());
            compiler.addLabeled(DLocalize.dmdRunConfigStringImportsLabel(), pathStringImports.getComponent());
            compiler.addLabeled(DLocalize.dmdRunConfigLinkerArgumentsLabel(), linkerArgs);
            for (final CheckBox checkBox : new CheckBox[] {
                cbRelease, cbDebug, cbUnitTest, cbLink, cbCoverageAnalysis, cbAllowDeprecated, cbIgnorePragmas,
                cbFunctionInlining, cbLibrary, cbNoArrayBoundsCheck, cbNoFloatingPointReferences, cbOptimize,
                cbEnforcePropertySyntax, cbQuiet, cbVerbose, cbListThreadLocalStorage, cbWarnings, cbInfoWarnings
            }) {
                compiler.addBottom(checkBox);
            }

            final FormBuilder output = FormBuilder.create();
            output.addBottom(cbGenerateDocumentation);
            output.addLabeled(DLocalize.dmdRunConfigDocumentationFolderLabel(), pathDocumentation.getComponent());
            output.addLabeled(DLocalize.dmdRunConfigDocumentationFilenameLabel(), filenameDocumentation);
            output.addLabeled(DLocalize.dmdRunConfigModuleDependenciesLabel(), filenameModuleDependencies);
            output.addBottom(cbGenerateHeader);
            output.addLabeled(DLocalize.dmdRunConfigHeaderDirectoryLabel(), pathHeaderDir.getComponent());
            output.addLabeled(DLocalize.dmdRunConfigHeaderFilenameLabel(), filenameHeader);
            output.addBottom(cbNoObjectFiles);
            output.addBottom(cbNoStripPaths);
            output.addBottom(cbGenerateJson);
            output.addLabeled(DLocalize.dmdRunConfigJsonFilenameLabel(), filenameJson);
            output.addBottom(cbGenerateMap);

            final FormBuilder debug = FormBuilder.create();
            debug.addBottom(cbAddSymbolicDebugInfo);
            debug.addBottom(cbGenerateStandardStackFrame);
            debug.addLabeled(DLocalize.dmdRunConfigSymbolicLibraryLabel(), textSymbolicLibrary);
            debug.addBottom(cbProfile);

            //wrap "textArgsPane" with a scrollable layout to allow scrolling.
            final VerticalLayout arguments = VerticalLayout.create();
            arguments.add(Label.create(DLocalize.dmdRunConfigActualArgumentsLabel()));
            arguments.add(ScrollableLayout.create(textArgsPane));
            final FormBuilder argumentsTab = FormBuilder.create();
            argumentsTab.addBottom(arguments);
            argumentsTab.addLabeled(DLocalize.dmdRunConfigDocumentationLabel(), linkDmdDoc);

            mainPanel.addTab(DLocalize.dmdRunConfigTabCompilation(), compiler.build());
            mainPanel.addTab(DLocalize.dmdRunConfigTabOutput(), output.build());
            mainPanel.addTab(DLocalize.dmdRunConfigTabDebug(), debug.build());
            mainPanel.addTab(DLocalize.dmdRunConfigTabArguments(), argumentsTab.build());
            mainPanel.addTab(DLocalize.dmdRunConfigTabProgram(), programLayout.getComponent());

            for (final ValueComponent<?> component : new ValueComponent<?>[] {
                comboModules, cbRelease, cbDebug, cbUnitTest, cbLink, cbCoverageAnalysis, cbAllowDeprecated,
                cbIgnorePragmas, cbFunctionInlining, cbLibrary, cbNoArrayBoundsCheck, cbNoFloatingPointReferences,
                cbOptimize, cbEnforcePropertySyntax, cbQuiet, cbVerbose, cbListThreadLocalStorage, cbWarnings,
                cbInfoWarnings, textDefaultLibrary, pathImports.getComponent(), pathStringImports.getComponent(),
                linkerArgs, cbGenerateDocumentation, filenameDocumentation, pathDocumentation.getComponent(),
                filenameModuleDependencies, cbGenerateHeader, pathHeaderDir.getComponent(), filenameHeader,
                cbGenerateMap, cbNoObjectFiles, cbNoStripPaths, cbGenerateJson, filenameJson, cbAddSymbolicDebugInfo,
                cbGenerateStandardStackFrame, textSymbolicLibrary, cbProfile
            }) {
                component.addValueListener(event -> fireEditorStateChanged());
            }
        }

        @RequiredUIAccess
        private FileChooserTextBoxBuilder.Controller createFolderField(final FileChooserDescriptor fcd) {
            return FileChooserTextBoxBuilder.create(project)
                .fileChooserDescriptor(fcd)
                .dialogTitle(DLocalize.dmdRunConfigSelectimportfolderTitle())
                .dialogDescription(DLocalize.dmdRunConfigSelectimportfolderDescription())
                .build();
        }

        @RequiredUIAccess
        private @Nullable Module getSelectedModule() {
            final String moduleName = comboModules.getValue();
            return moduleName == null ? null : ModuleManager.getInstance(project).findModuleByName(moduleName);
        }

        /* I hope there is no misprints in next methods :) */
        @RequiredUIAccess
        private void resetCompilerTabForm(final DlangRunDmdConfiguration config) {
            final Module module = config.getConfigurationModule().getModule();
            if (module != null) {
                comboModules.setValue(module.getName());
            }
            cbRelease.setValue(config.isRelease());
            cbDebug.setValue(config.isDebug());
            cbUnitTest.setValue(config.isUnitTest());
            cbLink.setValue(config.isLink());
            cbCoverageAnalysis.setValue(config.isCoverageAnalysis());
            cbAllowDeprecated.setValue(config.isAllowDeprecated());
            cbIgnorePragmas.setValue(config.isIgnorePragmas());
            cbFunctionInlining.setValue(config.isFunctionInlining());
            cbLibrary.setValue(config.isLibrary());
            cbNoArrayBoundsCheck.setValue(config.isNoArrayBoundsCheck());
            cbNoFloatingPointReferences.setValue(config.isNoFloatingPointReferences());
            cbOptimize.setValue(config.isOptimize());
            cbEnforcePropertySyntax.setValue(config.isEnforcePropertySyntax());
            cbQuiet.setValue(config.isQuiet());
            cbVerbose.setValue(config.isVerbose());
            cbListThreadLocalStorage.setValue(config.isListThreadLocalStorage());
            cbWarnings.setValue(config.isWarnings());
            cbInfoWarnings.setValue(config.isInfoWarnings());
            textDefaultLibrary.setValue(StringUtil.notNullize(config.getDefaultLibrary()));
            pathImports.setValue(StringUtil.notNullize(config.getImportsPath()));
            pathStringImports.setValue(StringUtil.notNullize(config.getStringImportsPath()));
            linkerArgs.setValue(StringUtil.notNullize(config.getLinkerArgs()));
        }

        @RequiredUIAccess
        private void resetOutputTabForm(final DlangRunDmdConfiguration config) {
            cbGenerateDocumentation.setValue(config.isGenerateDocs());
            filenameDocumentation.setValue(StringUtil.notNullize(config.getDocsFilename()));
            pathDocumentation.setValue(StringUtil.notNullize(config.getDocsPath()));
            filenameModuleDependencies.setValue(StringUtil.notNullize(config.getModuleDepsFilename()));
            cbGenerateHeader.setValue(config.isGenerateHeader());
            pathHeaderDir.setValue(StringUtil.notNullize(config.getHeaderDir()));
            filenameHeader.setValue(StringUtil.notNullize(config.getHeaderFilename()));
            cbGenerateMap.setValue(config.isGenerateMap());
            cbNoObjectFiles.setValue(config.isNoObjectFiles());
            cbNoStripPaths.setValue(config.isNoStripPaths());
            cbGenerateJson.setValue(config.isGenerateJson());
            filenameJson.setValue(StringUtil.notNullize(config.getJsonFilename()));
        }

        @RequiredUIAccess
        private void resetDebugTabForm(final DlangRunDmdConfiguration config) {
            cbAddSymbolicDebugInfo.setValue(config.isAddSymbolicDebugInfo());
            cbGenerateStandardStackFrame.setValue(config.isGenerateStandardStackFrame());
            textSymbolicLibrary.setValue(StringUtil.notNullize(config.getSymbolicLibrary()));
            cbProfile.setValue(config.isProfile());
        }

        /* Make "linkDmdDoc" clickable. */
        @RequiredUIAccess
        private Hyperlink setDocLink() {
            return Hyperlink.create(DLocalize.dmdRunConfigDocumentationLink(),
                event -> Platform.current().openInBrowser("https://dlang.org/dmd-linux.html#switches"));
        }

        @RequiredUIAccess
        private void applyCompilerTabForm(final DlangRunDmdConfiguration config) {
            config.setModuleName(comboModules.getValue());
            config.setRelease(cbRelease.getValueOrError());
            config.setDebug(cbDebug.getValueOrError());
            config.setUnitTest(cbUnitTest.getValueOrError());
            config.setLink(cbLink.getValueOrError());
            config.setCoverageAnalysis(cbCoverageAnalysis.getValueOrError());
            config.setAllowDeprecated(cbAllowDeprecated.getValueOrError());
            config.setIgnorePragmas(cbIgnorePragmas.getValueOrError());
            config.setFunctionInlining(cbFunctionInlining.getValueOrError());
            config.setLibrary(cbLibrary.getValueOrError());
            config.setNoArrayBoundsCheck(cbNoArrayBoundsCheck.getValueOrError());
            config.setNoFloatingPointReferences(cbNoFloatingPointReferences.getValueOrError());
            config.setOptimize(cbOptimize.getValueOrError());
            config.setEnforcePropertySyntax(cbEnforcePropertySyntax.getValueOrError());
            config.setQuiet(cbQuiet.getValueOrError());
            config.setVerbose(cbVerbose.getValueOrError());
            config.setListThreadLocalStorage(cbListThreadLocalStorage.getValueOrError());
            config.setWarnings(cbWarnings.getValueOrError());
            config.setInfoWarnings(cbInfoWarnings.getValueOrError());
            config.setDefaultLibrary(textDefaultLibrary.getValue());
            config.setImportsPath(pathImports.getValue());
            config.setStringImportsPath(pathStringImports.getValue());
            config.setLinkerArgs(linkerArgs.getValue());
        }

        @RequiredUIAccess
        private void applyOutputTabForm(final DlangRunDmdConfiguration config) {
            config.setGenerateDocs(cbGenerateDocumentation.getValueOrError());
            config.setDocsFilename(filenameDocumentation.getValue());
            config.setDocsPath(pathDocumentation.getValue());
            config.setModuleDepsFilename(filenameModuleDependencies.getValue());
            config.setGenerateHeader(cbGenerateHeader.getValueOrError());
            config.setHeaderDir(pathHeaderDir.getValue());
            config.setHeaderFilename(filenameHeader.getValue());
            config.setGenerateMap(cbGenerateMap.getValueOrError());
            config.setNoObjectFiles(cbNoObjectFiles.getValueOrError());
            config.setNoStripPaths(cbNoStripPaths.getValueOrError());
            config.setGenerateJson(cbGenerateJson.getValueOrError());
            config.setJsonFilename(filenameJson.getValue());
        }

        @RequiredUIAccess
        private void applyDebugTabForm(final DlangRunDmdConfiguration config) {
            config.setAddSymbolicDebugInfo(cbAddSymbolicDebugInfo.getValueOrError());
            config.setGenerateStandardStackFrame(cbGenerateStandardStackFrame.getValueOrError());
            config.setSymbolicLibrary(textSymbolicLibrary.getValue());
            config.setProfile(cbProfile.getValueOrError());
        }

        /**
         * Update "textArgsPane" text area with actual DMD command line arguments
         */
        @RequiredUIAccess
        private void fillArguments(final DlangRunDmdConfiguration config) {
            final Module module = getSelectedModule();
            if (module == null) {
                return;
            }
            try {
                final java.util.List<String> args = DlangDmdConfigToArgsConverter.getDmdParameters(config, module);
                textArgsPane.setValue(StringUtil.join(args, "\n"));
            } catch (final NoSourcesException e) {
                LOG.warn("Cannot correctly populate textArgsPane in DMD config page: " + e.getMessage());
                textArgsPane.setValue(e.getMessage());
            } catch (final ExecutionException e) {
                LOG.error("There was a problem filling the textArgsPane in DMD config page", e);
                textArgsPane.setValue("*Exception*:\n" + e.getMessage());
            }
        }
    }
}

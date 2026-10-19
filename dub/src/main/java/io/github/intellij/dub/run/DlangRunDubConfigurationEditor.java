package io.github.intellij.dub.run;

import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.run.NativeProgramParametersLayout;
import consulo.project.Project;
import consulo.ui.CheckBox;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Port of upstream {@code DlangRunDubConfigurationEditor.kt} (a Swing form there) on Consulo UI. The working directory,
 * the environment and the program arguments are nativeDev's common program parameters.
 */
public class DlangRunDubConfigurationEditor extends SettingsEditor<DlangRunDubConfiguration> {
    private static final int BUILD = 0;
    private static final int RUN = 1;
    private static final int TEST = 2;

    private final Project myProject;
    private @Nullable Layout myLayout;

    public DlangRunDubConfigurationEditor(Project project) {
        myProject = project;
    }

    @RequiredUIAccess
    @Override
    protected Component createUIComponent() {
        Layout layout = new Layout(myProject);
        layout.build();
        myLayout = layout;
        return layout.getComponent();
    }

    /**
     * Update editor UI with data of DLangRunDubConfiguration.
     * All components must be changed according to "config" data.
     */
    @RequiredUIAccess
    @Override
    protected void resetEditorFrom(DlangRunDubConfiguration configuration) {
        Layout layout = myLayout;
        if (layout != null) {
            layout.reset(configuration);
        }
    }

    /**
     * Save state of editor UI to DLangRunDubConfiguration instance.
     */
    @RequiredUIAccess
    @Override
    protected void applyEditorTo(DlangRunDubConfiguration configuration) {
        Layout layout = myLayout;
        if (layout != null) {
            layout.apply(configuration);
        }
    }

    private static class Layout extends NativeProgramParametersLayout<DlangRunDubConfiguration> {
        //General tab
        private final ComboBox<String> myModules;
        private final ComboBox<Integer> myGeneralDubOptions;

        // Common
        private final CheckBox myCbRdmd = CheckBox.create(DLocalize.dubRunOptionRdmd());
        private final CheckBox myCbForce = CheckBox.create(DLocalize.dubRunOptionForce());
        private final CheckBox myCbNoDeps = CheckBox.create(DLocalize.dubRunOptionNodeps());
        private final CheckBox myCbForceRemove = CheckBox.create(DLocalize.dubRunOptionForceRemove());
        private final CheckBox myCbCombined = CheckBox.create(DLocalize.dubRunOptionCombined());
        private final CheckBox myCbParallel = CheckBox.create(DLocalize.dubRunOptionParallel());
        private final TextBox myTfBuild = TextBox.create();
        private final TextBox myTfConfig = TextBox.create();
        private final TextBox myTfArch = TextBox.create();
        private final TextBox myTfDebug = TextBox.create();
        private final TextBox myTfCompiler = TextBox.create();
        private final ComboBox<Integer> myTfBuildMode;
        private final CheckBox myCbVerbose = CheckBox.create(DLocalize.dubRunOptionVerbose());
        private final CheckBox myCbQuiet = CheckBox.create(DLocalize.dubRunOptionQuiet());
        private final TextBox myAdditionalParams = TextBox.create();

        // Run
        private final CheckBox myCbTempBuild = CheckBox.create(DLocalize.dubRunOptionTempBuild());

        // Test
        private final TextBox myTfMainFile = TextBox.create();
        private final CheckBox myCbCoverage = CheckBox.create(DLocalize.dubRunOptionCoverage());

        @RequiredUIAccess
        private Layout(Project project) {
            super(project);
            ComboBox.Builder<String> modules = ComboBox.builder();
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                if (module.getExtension(DModuleExtension.class) != null) {
                    modules.add(module.getName(), LocalizeValue.of(module.getName()));
                }
            }
            myModules = modules.build();

            myGeneralDubOptions = ComboBox.<Integer>builder()
                .add(BUILD, DLocalize.dubRunCommandBuild())
                .add(RUN, DLocalize.dubRunCommandRun())
                .add(TEST, DLocalize.dubRunCommandTest())
                .build();
            myGeneralDubOptions.addValueListener(event -> updateEnabled());

            myTfBuildMode = ComboBox.<Integer>builder()
                .add(0, DLocalize.dubRunBuildModeSeparate())
                .add(1, DLocalize.dubRunBuildModeAllAtOnce())
                .add(2, DLocalize.dubRunBuildModeSingleFile())
                .build();
        }

        @RequiredUIAccess
        @Override
        protected void addBefore(FormBuilder builder) {
            builder.addLabeled(DLocalize.dubRunModuleLabel(), myModules);
            builder.addLabeled(DLocalize.dubRunCommandLabel(), myGeneralDubOptions);
            builder.addLabeled(DLocalize.dubRunBuildLabel(), myTfBuild);
            builder.addLabeled(DLocalize.dubRunConfigLabel(), myTfConfig);
            builder.addLabeled(DLocalize.dubRunArchLabel(), myTfArch);
            builder.addLabeled(DLocalize.dubRunDebugLabel(), myTfDebug);
            builder.addLabeled(DLocalize.dubRunCompilerLabel(), myTfCompiler);
            builder.addLabeled(DLocalize.dubRunBuildModeLabel(), myTfBuildMode);
            builder.addLabeled(DLocalize.dubRunMainFileLabel(), myTfMainFile);
            builder.addLabeled(DLocalize.dubRunParametersLabel(), myAdditionalParams);
            builder.addBottom(myCbRdmd);
            builder.addBottom(myCbForce);
            builder.addBottom(myCbNoDeps);
            builder.addBottom(myCbForceRemove);
            builder.addBottom(myCbCombined);
            builder.addBottom(myCbParallel);
            builder.addBottom(myCbTempBuild);
            builder.addBottom(myCbCoverage);
            builder.addBottom(myCbQuiet);
            builder.addBottom(myCbVerbose);
        }

        @RequiredUIAccess
        private void updateEnabled() {
            Integer value = myGeneralDubOptions.getValue();
            int option = value == null ? RUN : value;
            boolean inBuildState = option == BUILD;
            boolean inRunState = option == RUN;
            boolean inTestState = option == TEST;
            myCbTempBuild.setEnabled(inRunState);
            myCbCoverage.setEnabled(inTestState);
            myTfMainFile.setEnabled(inTestState);
            myCbRdmd.setEnabled(inBuildState || inRunState);
            myCbParallel.setEnabled(inBuildState || inRunState);
        }

        @RequiredUIAccess
        @Override
        public void reset(DlangRunDubConfiguration config) {
            super.reset(config);
            Module module = config.getConfigurationModule().getModule();
            if (module != null) {
                myModules.setValue(module.getName());
            }
            myGeneralDubOptions.setValue(config.getGeneralDubOptions());
            myCbRdmd.setValue(config.isCbRdmd());
            myCbNoDeps.setValue(config.isCbNoDeps());
            myCbForce.setValue(config.isCbForce());
            myCbForceRemove.setValue(config.isCbForceRemove());
            myCbCombined.setValue(config.isCbCombined());
            myCbParallel.setValue(config.isCbParallel());
            myCbVerbose.setValue(config.isVerbose());
            myCbQuiet.setValue(config.isQuiet());
            myTfBuild.setValue(nullize(config.getTfBuild()));
            myTfConfig.setValue(nullize(config.getTfConfig()));
            myTfArch.setValue(nullize(config.getTfArch()));
            myTfDebug.setValue(nullize(config.getTfDebug()));
            myTfCompiler.setValue(nullize(config.getTfCompiler()));
            myTfBuildMode.setValue(config.getBuildMode());
            myCbTempBuild.setValue(config.isCbTempBuild());
            myTfMainFile.setValue(nullize(config.getTfMainFile()));
            myCbCoverage.setValue(config.isCbCoverage());
            myAdditionalParams.setValue(nullize(config.getAdditionalParams()));
            updateEnabled();
        }

        @RequiredUIAccess
        @Override
        public void apply(DlangRunDubConfiguration config) {
            super.apply(config);
            config.setModuleName(myModules.getValue());
            Integer option = myGeneralDubOptions.getValue();
            config.setGeneralDubOptions(option == null ? RUN : option);
            config.setCbRdmd(myCbRdmd.getValueOrError());
            config.setCbNoDeps(myCbNoDeps.getValueOrError());
            config.setCbForce(myCbForce.getValueOrError());
            config.setCbForceRemove(myCbForceRemove.getValueOrError());
            config.setCbCombined(myCbCombined.getValueOrError());
            config.setCbParallel(myCbParallel.getValueOrError());
            config.setTfBuild(myTfBuild.getValue());
            config.setTfConfig(myTfConfig.getValue());
            config.setTfArch(myTfArch.getValue());
            config.setTfDebug(myTfDebug.getValue());
            config.setTfCompiler(myTfCompiler.getValue());
            Integer buildMode = myTfBuildMode.getValue();
            config.setBuildMode(buildMode == null ? 0 : buildMode);
            config.setVerbose(myCbVerbose.getValueOrError());
            config.setQuiet(myCbQuiet.getValueOrError());
            config.setCbTempBuild(myCbTempBuild.getValueOrError());
            config.setCbCoverage(myCbCoverage.getValueOrError());
            config.setTfMainFile(myTfMainFile.getValue());
            config.setAdditionalParams(myAdditionalParams.getValue());
        }

        private static String nullize(@Nullable String value) {
            return value == null ? "" : value;
        }
    }
}

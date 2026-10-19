package io.github.intellij.dlanguage.run;

import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nativeDev.debugger.run.NativeProgramParametersLayout;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Consulo: built on nativeDev's {@link NativeProgramParametersLayout} instead of upstream's Swing form; the program
 * parameters, the working directory and the environment variables are its common fields.
 */
public class DlangRunAppConfigurationEditor extends SettingsEditor<DlangRunAppConfiguration> {
    private final Project project;
    private @Nullable Layout layout;

    public DlangRunAppConfigurationEditor(final Project project) {
        this.project = project;
    }

    /**
     * Update editor UI with data of DLangRunAppConfiguration.
     * All components must be changed according to "config" data.
     */
    @RequiredUIAccess
    @Override
    protected void resetEditorFrom(final DlangRunAppConfiguration config) {
        final Layout layout = this.layout;
        if (layout != null) {
            layout.reset(config);
        }
    }


    /**
     * Save state of editor UI to DLangRunAppConfiguration instance.
     */
    @RequiredUIAccess
    @Override
    protected void applyEditorTo(final DlangRunAppConfiguration config) {
        final Layout layout = this.layout;
        if (layout != null) {
            layout.apply(config);
        }
    }

    @RequiredUIAccess
    @Override
    protected Component createUIComponent() {
        final Layout layout = new Layout(project);
        layout.build();
        this.layout = layout;
        return layout.getComponent();
    }

    private static class Layout extends NativeProgramParametersLayout<DlangRunAppConfiguration> {
        private final ComboBox<String> comboModule;
        private final Label appPathLabel = Label.create();

        @RequiredUIAccess
        private Layout(final Project project) {
            super(project);
            final ComboBox.Builder<String> modules = ComboBox.builder();
            for (final Module module : ModuleManager.getInstance(project).getModules()) {
                if (module.getExtension(DModuleExtension.class) != null) {
                    modules.add(module.getName(), LocalizeValue.of(module.getName()));
                }
            }
            comboModule = modules.build();
        }

        @RequiredUIAccess
        @Override
        protected void addBefore(final FormBuilder builder) {
            builder.addLabeled(DLocalize.runConfigModuleLabel(), comboModule);
            builder.addLabeled(DLocalize.runAppExecutableLabel(), appPathLabel);
        }

        @RequiredUIAccess
        @Override
        public void reset(final DlangRunAppConfiguration config) {
            super.reset(config);
            final Module module = config.getConfigurationModule().getModule();
            if (module != null) {
                comboModule.setValue(module.getName());
            }
            appPathLabel.setText(LocalizeValue.of(config.getExecutablePath()));
        }

        @RequiredUIAccess
        @Override
        public void apply(final DlangRunAppConfiguration config) {
            super.apply(config);
            config.setModuleName(comboModule.getValue());
        }
    }
}

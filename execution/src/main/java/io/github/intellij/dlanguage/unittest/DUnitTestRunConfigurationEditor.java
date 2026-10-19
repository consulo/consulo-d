package io.github.intellij.dlanguage.unittest;

import consulo.d.localize.DLocalize;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.localize.ExecutionLocalize;
import consulo.execution.ui.awt.EnvironmentVariablesTextFieldWithBrowseButton;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DlangFileType;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Consulo: built with Consulo UI instead of upstream's Swing form. The D file is chosen with a file chooser limited to
 * D files outside the SDK and packages, where upstream used IntelliJ's {@code TreeFileChooser}.
 */
public class DUnitTestRunConfigurationEditor extends SettingsEditor<DUnitTestRunConfiguration> {

    private final Project project;

    private @Nullable Form form;

    public DUnitTestRunConfigurationEditor(final Project project) {
        this.project = project;
    }

    @RequiredUIAccess
    private static FileChooserTextBoxBuilder.Controller initDFileTextWithBrowse(final Project project) {
        return FileChooserTextBoxBuilder.create(project)
            .dialogTitle(DLocalize.chooseDlanguageMainFile())
            .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                .withFileFilter(file -> file.getFileType() == DlangFileType.INSTANCE &&
                    !DUnitTestRunConfigurationProducer.isInDLanguageSdkOrDLanguagePackagesFolder(project, file)))
            .build();
    }

    @RequiredUIAccess
    @Override
    protected void resetEditorFrom(final DUnitTestRunConfiguration config) {
        final Form form = this.form;
        if (form == null) {
            return;
        }
        final String workingDir = config.getWorkingDir();

        if(StringUtil.isNotEmpty(workingDir)) {
            form.myWorkingDirectory.setValue(workingDir);
        } else {
            form.myWorkingDirectory.setValue(StringUtil.notNullize(this.project.getBasePath()));
        }

        form.myFileField.setValue(StringUtil.notNullize(config.getdFilePath()));

        final Map<String, String> envVars = config.getEnvVars();
        if (envVars != null) {
            form.envVariables.setEnvs(config.getEnvVars());
        }
    }

    @RequiredUIAccess
    @Override
    protected void applyEditorTo(final DUnitTestRunConfiguration config) {
        final Form form = this.form;
        if (form == null) {
            return;
        }
        config.setEnvVars(form.envVariables.getEnvs());
        config.setWorkingDir(form.myWorkingDirectory.getValue());
        config.setdFilePath(FileUtil.toSystemIndependentName(form.myFileField.getValue().trim()));
    }

    @RequiredUIAccess
    @Override
    protected Component createUIComponent() {
        final Form form = new Form();
        this.form = form;
        return form.myMainPanel;
    }

    private class Form {
        private final FileChooserTextBoxBuilder.Controller myFileField;
        private final FileChooserTextBoxBuilder.Controller myWorkingDirectory;
        private final EnvironmentVariablesTextFieldWithBrowseButton envVariables = new EnvironmentVariablesTextFieldWithBrowseButton();
        private final Component myMainPanel;

        @RequiredUIAccess
        Form() {
            myFileField = initDFileTextWithBrowse(project);

            myWorkingDirectory = FileChooserTextBoxBuilder.create(project)
                .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFolderDescriptor())
                .dialogTitle(ExecutionLocalize.selectWorkingDirectoryMessage())
                .dialogDescription(LocalizeValue.empty())
                .build();

            final FormBuilder builder = FormBuilder.create();
            builder.addLabeled(DLocalize.dunitRunConfigurationFileLabel(), myFileField.getComponent());
            builder.addLabeled(DLocalize.dunitRunConfigurationWorkingDirectoryLabel(), myWorkingDirectory.getComponent());
            builder.addLabeled(DLocalize.dunitRunConfigurationEnvironmentVariablesLabel(), envVariables.getComponent());
            myMainPanel = builder.build();
        }
    }
}

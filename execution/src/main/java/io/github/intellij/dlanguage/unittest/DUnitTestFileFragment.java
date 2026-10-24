package io.github.intellij.dlanguage.unittest;

import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DlangFileType;

/**
 * The D file whose unit tests run; the chooser offers D files outside the SDK and the packages.
 */
public class DUnitTestFileFragment implements RunConfigurationFragment<DUnitTestRunConfiguration> {
    @Override
    public String getId() {
        return "dunit.file";
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.runConfigurationFragmentDunitFile();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<DUnitTestRunConfiguration> createEditor(Project project, Disposable parent) {
        FileChooserTextBoxBuilder.Controller file = FileChooserTextBoxBuilder.create(project)
            .dialogTitle(DLocalize.chooseDlanguageMainFile())
            .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                .withFileFilter(it -> it.getFileType() == DlangFileType.INSTANCE &&
                    !DUnitTestRunConfigurationProducer.isInDLanguageSdkOrDLanguagePackagesFolder(project, it)))
            .uiDisposable(parent)
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(DLocalize.dunitRunConfigurationFileLabel(), file.getComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(DUnitTestRunConfiguration target) {
                file.setValue(StringUtil.notNullize(target.getdFilePath()));
            }

            @RequiredUIAccess
            @Override
            public void apply(DUnitTestRunConfiguration target) {
                target.setdFilePath(FileUtil.toSystemIndependentName(file.getValue().trim()));
            }
        };
    }
}

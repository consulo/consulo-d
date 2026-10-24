package io.github.intellij.dlanguage.run;

import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * The executable the selected module compiles to, which the configuration runs.
 */
public class DlangRunAppExecutableFragment implements RunConfigurationFragment<DlangRunAppConfiguration> {
    @Override
    public String getId() {
        return "dlangApp.executable";
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.runConfigurationFragmentAppExecutable();
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
    public RunConfigurationFragmentEditor<DlangRunAppConfiguration> createEditor(Project project, Disposable parent) {
        Label executable = Label.create();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(DLocalize.runAppExecutableLabel(), executable);
            }

            @RequiredUIAccess
            @Override
            public void reset(DlangRunAppConfiguration target) {
                executable.setText(LocalizeValue.of(target.getExecutablePath()));
            }

            @RequiredUIAccess
            @Override
            public void apply(DlangRunAppConfiguration target) {
            }
        };
    }
}

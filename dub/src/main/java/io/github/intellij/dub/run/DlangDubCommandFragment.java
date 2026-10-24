package io.github.intellij.dub.run;

import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * The DUB command to run: {@code build}, {@code run} or {@code test}.
 */
public class DlangDubCommandFragment implements RunConfigurationFragment<DlangRunDubConfiguration> {
    private static final int BUILD = 0;
    private static final int RUN = 1;
    private static final int TEST = 2;

    @Override
    public String getId() {
        return "dub.command";
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.runConfigurationFragmentDubCommand();
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
    public RunConfigurationFragmentEditor<DlangRunDubConfiguration> createEditor(Project project, Disposable parent) {
        ComboBox<Integer> command = ComboBox.<Integer>builder()
            .add(BUILD, DLocalize.dubRunCommandBuild())
            .add(RUN, DLocalize.dubRunCommandRun())
            .add(TEST, DLocalize.dubRunCommandTest())
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(DLocalize.dubRunCommandLabel(), command);
            }

            @RequiredUIAccess
            @Override
            public void reset(DlangRunDubConfiguration target) {
                command.setValue(target.getGeneralDubOptions());
            }

            @RequiredUIAccess
            @Override
            public void apply(DlangRunDubConfiguration target) {
                Integer value = command.getValue();
                target.setGeneralDubOptions(value == null ? RUN : value);
            }
        };
    }
}

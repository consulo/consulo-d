package io.github.intellij.dub.run;

import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * The {@code --build-mode} of DUB, visible when it is not {@code separate}.
 */
public class DlangDubBuildModeFragment implements RunConfigurationFragment<DlangRunDubConfiguration> {
    private static final int SEPARATE = 0;
    private static final int ALL_AT_ONCE = 1;
    private static final int SINGLE_FILE = 2;

    @Override
    public String getId() {
        return "dub.buildMode";
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.runConfigurationFragmentDubBuildMode();
    }

    @Override
    public LocalizeValue getGroup() {
        return DLocalize.runConfigurationFragmentGroupDub();
    }

    @Override
    public boolean isVisibleByDefault(DlangRunDubConfiguration target) {
        return target.getBuildMode() != SEPARATE;
    }

    @Override
    public void applyHidden(DlangRunDubConfiguration target) {
        target.setBuildMode(SEPARATE);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<DlangRunDubConfiguration> createEditor(Project project, Disposable parent) {
        ComboBox<Integer> buildMode = ComboBox.<Integer>builder()
            .add(SEPARATE, DLocalize.dubRunBuildModeSeparate())
            .add(ALL_AT_ONCE, DLocalize.dubRunBuildModeAllAtOnce())
            .add(SINGLE_FILE, DLocalize.dubRunBuildModeSingleFile())
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(DLocalize.dubRunBuildModeLabel(), buildMode);
            }

            @RequiredUIAccess
            @Override
            public void reset(DlangRunDubConfiguration target) {
                buildMode.setValue(target.getBuildMode());
            }

            @RequiredUIAccess
            @Override
            public void apply(DlangRunDubConfiguration target) {
                Integer value = buildMode.getValue();
                target.setBuildMode(value == null ? SEPARATE : value);
            }
        };
    }
}

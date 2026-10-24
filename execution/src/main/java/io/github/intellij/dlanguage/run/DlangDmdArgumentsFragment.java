package io.github.intellij.dlanguage.run;

import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.ui.Hyperlink;
import consulo.ui.Label;
import consulo.ui.TextArea;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.run.exception.NoSourcesException;

import java.util.List;

/**
 * The DMD arguments the options produce, as of the last change of the page; hidden by default.
 */
public class DlangDmdArgumentsFragment implements RunConfigurationFragment<DlangRunDmdConfiguration> {
    private static final Logger LOG = Logger.getInstance(DlangDmdArgumentsFragment.class);

    @Override
    public String getId() {
        return "dmd.arguments";
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.runConfigurationFragmentDmdArguments();
    }

    @Override
    public boolean isVisibleByDefault(DlangRunDmdConfiguration target) {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<DlangRunDmdConfiguration> createEditor(Project project, Disposable parent) {
        TextArea arguments = TextArea.create();
        arguments.setEditable(false);

        VerticalLayout panel = VerticalLayout.create();
        panel.add(Label.create(DLocalize.dmdRunConfigActualArgumentsLabel()));
        panel.add(ScrollableLayout.create(arguments));
        panel.add(Hyperlink.create(DLocalize.dmdRunConfigDocumentationLink(),
            event -> Platform.current().openInBrowser("https://dlang.org/dmd-linux.html#switches")));

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addBottom(panel);
            }

            @RequiredUIAccess
            @Override
            public void reset(DlangRunDmdConfiguration target) {
                Module module = target.getConfigurationModule().getModule();
                if (module == null) {
                    arguments.setValue("");
                    return;
                }
                try {
                    List<String> args = DlangDmdConfigToArgsConverter.getDmdParameters(target, module);
                    arguments.setValue(StringUtil.join(args, "\n"));
                }
                catch (NoSourcesException e) {
                    arguments.setValue(StringUtil.notNullize(e.getMessage()));
                }
                catch (ExecutionException e) {
                    LOG.error("There was a problem computing the DMD arguments", e);
                    arguments.setValue("*Exception*:\n" + e.getMessage());
                }
            }

            @RequiredUIAccess
            @Override
            public void apply(DlangRunDmdConfiguration target) {
            }
        };
    }
}

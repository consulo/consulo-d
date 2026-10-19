package io.github.intellij.dub.actions;

import consulo.annotation.component.ActionImpl;
import consulo.d.localize.DLocalize;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;

/**
 * @author Samael Bate (singingbush)
 * created on 01/04/2017.
 */
@ActionImpl(id = "dub.settings.action")
public class ConfigureDToolsAction extends DubAction {

    /**
     * The id of the "D Tools" settings ({@code DLanguageToolsConfigurable.D_TOOLS_ID} upstream).
     */
    private static final String D_TOOLS_CONFIGURABLE_ID = "D Tools";

    private static final Logger LOG = Logger.getInstance(ConfigureDToolsAction.class);

    public ConfigureDToolsAction() {
        super(DLocalize.actionDubSettingsText(), LocalizeValue.empty(), PlatformIconGroup.generalGearplain());
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        LOG.info("dub settings action performed");

        Project project = e.getData(Project.KEY);

        if (project != null) {
            ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, D_TOOLS_CONFIGURABLE_ID, "");
        }
    }
}

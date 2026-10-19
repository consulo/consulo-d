package io.github.intellij.dlanguage.utils;

import consulo.application.dumb.DumbAware;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import io.github.intellij.dlanguage.settings.DLanguageToolsConfigurable;

/**
 * Port of upstream {@code DToolsNotificationAction.kt}. Consulo updates actions through {@link AnActionWithSyncUpdate}.
 */
public class DToolsNotificationAction extends AnAction implements AnActionWithSyncUpdate, DumbAware {
    public DToolsNotificationAction(LocalizeValue text) {
        super(text);
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(Project.KEY) != null);
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getData(Project.KEY), DLanguageToolsConfigurable.D_TOOLS_ID);
    }
}

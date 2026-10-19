package io.github.intellij.dub.actions;

import consulo.d.module.extension.DModuleExtension;
import consulo.localize.LocalizeValue;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.image.Image;
import io.github.intellij.dub.service.DubBinaryPathProvider;

/**
 * If you intend to use AnAction with dub, use this base class instead. It will ensure that dub is available
 * @author Samael Bate (singingbush)
 */
public abstract class DubAction extends DumbAwareAction implements AnActionWithSyncUpdate {

    protected DubAction(LocalizeValue text, LocalizeValue description, Image icon) {
        super(text, description, icon);
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(canUseDub(e));
    }

    // Consulo has no module types: a D module is a module with the D extension
    private boolean canUseDub(AnActionEvent e) {
        Project project = e.getData(Project.KEY);
        if (project == null) {
            return false;
        }
        return DubBinaryPathProvider.INSTANCE.isDubAvailable()
            && ModuleExtensionHelper.getInstance(project).hasModuleExtension(DModuleExtension.class);
    }

}

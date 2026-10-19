package consulo.d.dub.action;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.dumb.DumbAware;
import consulo.d.localize.DLocalize;
import consulo.localize.LocalizeValue;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.IdeActions;
import io.github.intellij.dub.Dub;
import io.github.intellij.dub.actions.DubRepoUsageAction;

/**
 * Upstream's {@code dlang-dub-tools-menu-group} of the Tools menu. Its {@code ProcessDLibs} action is gone: the DUB
 * import resolves the dependencies.
 */
@ActionImpl(
    id = "dlang-dub-tools-menu-group",
    children = @ActionRef(type = DubRepoUsageAction.class),
    parents = @ActionParentRef(value = @ActionRef(id = IdeActions.TOOLS_MENU), anchor = ActionRefAnchor.LAST)
)
public class DubToolsMenuGroup extends DefaultActionGroup implements DumbAware {
    public DubToolsMenuGroup() {
        super(DLocalize.actionDubToolsMenuGroupText(), LocalizeValue.empty(), Dub.BALL);
        setPopup(true);
    }
}

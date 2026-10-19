package io.github.intellij.dub.toolwindow;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposer;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowFactory;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentFactory;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dub.Dub;

/**
 * Provides a tool window for dub (displayed on right-hand side).
 * @author Samael Bate (singingbush)
 * Created on 29/07/2016.
 *
 * for an example ToolWindow check out IDEtalkToolWindow in IDEtalk, see: https://github.com/JetBrains/intellij-plugins/
 * or the tool_window code sample in https://github.com/JetBrains/intellij-sdk-docs/
 *
 * but best example is the gradle plugin in https://github.com/JetBrains/intellij-community
 *
 * see
 */
@ExtensionImpl
public class DubToolWindow implements ToolWindowFactory {

    public static final String ID = "DubToolBar";

    private static final Logger LOG = Logger.getInstance(DubToolWindow.class);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dubToolWindowName();
    }

    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Override
    public Image getIcon() {
        return Dub.BALL;
    }

    @RequiredUIAccess
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        LOG.debug("Creating DUB Tool Window: " + toolWindow.getTitle());

        //val modules = io.github.intellij.dlanguage.module.DlangModuleType.findModules(project)
        //log.info("we have modules")

        DubToolWindowPanel dtw = new DubToolWindowPanel(project, toolWindow);
        Content content = ContentFactory.getInstance().createUIContent(dtw.getComponent(), "", false);
        toolWindow.getContentManager().addContent(content);
        Disposer.register(project, dtw);
    }

    @Override
    public boolean validate(Project project) {
        LOG.debug("DUB Tool Window Condition: value(" + project.getName() + ")");

        VirtualFile projectDir = project.getBaseDir();
        VirtualFile[] files = projectDir != null ? projectDir.getChildren() : null;
        if (files == null) {
            return false;
        }

        for (VirtualFile f : files) {
            if (!f.isDirectory() && ("dub.json".equalsIgnoreCase(f.getName()) || "dub.sdl".equalsIgnoreCase(f.getName()))) {
                return true;
            }
        }
        return false;
    }
}

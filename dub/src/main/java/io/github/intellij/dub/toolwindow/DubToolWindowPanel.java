package io.github.intellij.dub.toolwindow;

import consulo.d.dub.orderEntry.DubDependencyEntries;
import consulo.d.dub.orderEntry.DubDependencyOrderEntryModel;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.disposer.Disposable;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.RunManager;
import consulo.externalSystem.importing.ImportSpecBuilder;
import consulo.externalSystem.service.project.ExternalSystemProjectRefresher;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.TextAttribute;
import consulo.ui.TextItemPresentation;
import consulo.ui.Tree;
import consulo.ui.TreeModel;
import consulo.ui.TreeNode;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.ToggleAction;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.style.StandardColors;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dub.Dub;
import io.github.intellij.dub.actions.ConfigureDToolsAction;
import io.github.intellij.dub.actions.DubBuildAction;
import io.github.intellij.dub.run.DlangRunDubConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Samael Bate (singingbush)
 * Created on 31/03/2017.
 *
 * should this extend ExternalProjectsViewImpl or SimpleToolWindowPanel ???
 * <p>
 * Consulo: the panel is built with the unified UI (a {@link DockLayout} with the toolbar on top of a {@link Tree}).
 * Upstream rebuilt it on its own tool settings and dub file topics; here it is rebuilt when the module roots change,
 * which is what a DUB import does.
 */
public class DubToolWindowPanel implements Disposable {

    private static final Logger LOG = Logger.getInstance(DubToolWindowPanel.class);

    private final Project myProject;
    private final ToolWindow myToolWindow;
    private final DockLayout myRoot;

    @RequiredUIAccess
    public DubToolWindowPanel(Project project, ToolWindow toolWindow) {
        myProject = project;
        myToolWindow = toolWindow;

        LOG.info("the DubToolWindowPanel has been created");

        myRoot = DockLayout.create();
        myRoot.top(createToolbarPanel());

        createToolbarContent();

        project.getMessageBus().connect(this).subscribe(ModuleRootListener.class, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                createToolbarContent();
            }
        });
    }

    public Project getProject() {
        return myProject;
    }

    public ToolWindow getToolWindow() {
        return myToolWindow;
    }

    public Component getComponent() {
        return myRoot;
    }

    @RequiredUIAccess
    private void createToolbarContent() {
        DubTreeNode root = new DubTreeNode(myProject.getName()); // ProjectNode()
//        val root = ProjectViewProjectNode(project, ViewSettings.DEFAULT)

        // display the project roots (SDK)
        ProjectRootManager.getInstance(myProject).orderEntries()
            .forEach(oe -> {
                if (oe instanceof LibraryOrderEntry || oe instanceof ModuleExtensionWithSdkOrderEntry) {
                    root.add(new DubTreeNode(oe.getPresentableName()));
                }
                return true;
            });

        // There don't seem to be any dub dependencies available this way (probably should be)
        //ProjectRootManager.getInstance(project).orderEntries()
        //    .forEachLibrary {
        //        root.add(DefaultMutableTreeNode(it.presentableName))
        //        return@forEachLibrary true
        //    }

        // then for every sub module would need to add:
        List<Module> dlangModules = new ArrayList<>();
        for (DModuleExtension extension : ModuleExtensionHelper.getInstance(myProject).getModuleExtensions(DModuleExtension.class)) {
            dlangModules.add(extension.getModule());
        }
        for (Module module : dlangModules) {
            DubTreeNode dubModule = new DubTreeNode(module); // ModuleNode(it, null)

            ModuleRootManager.getInstance(module)
                .orderEntries() // order entries includes theSDK and module source
                .forEach(oe -> {
                    // the order entry (oe) could be a ModuleOrderEntryBridge, LibraryOrderEntryBridge,
                    // SdkOrderEntryBridge, InheritedSdkOrderEntryBridge, or a ModuleSourceOrderEntryBridge.
                    //val libNode = DefaultMutableTreeNode(oe.presentableName)
                    //libNode.userObject = oe // setting the userObject messes up the UI
                    //PackageViewProjectNode(project, ViewSettings.DEFAULT)
                    dubModule.add(new DubTreeNode(oe.getPresentableName()));
                    return oe.isValid();
                });

            root.add(dubModule);
        }

        // Find Run Configurations for DUB and show them in the Tool Window:
        DubTreeNode dubTasks = new DubTreeNode(DLocalize.dubToolWindowRunConfigurations()); // should this be: ExternalSystemTasksTree

        // List all the dub based run configurations that are configured for the current project
        // todo: work out how to right-click on this and run it (perhaps in the DubTreeRenderer)
        for (RunnerAndConfigurationSettings settings : RunManager.getInstance(myProject).getAllSettings()) {
            if (settings.getConfiguration() instanceof DlangRunDubConfiguration) {
                dubTasks.add(new RunnerAndConfigurationSettingsNode(settings));
            }
        }

        root.add(dubTasks);

        // Finally list the project dependencies.
        // todo: Show transient dependencies under the dependency that brought them in.
        // Consulo: the dependencies are the DUB packages the DUB import added to the D modules, not "dub describe" output
        DubTreeNode dependenciesRootNode = new DubTreeNode(DependenciesNode.INSTANCE);
        for (Module module : dlangModules) {
            ModuleRootManager.getInstance(module).orderEntries().forEach(oe -> {
                DubDependencyOrderEntryModel dependency = DubDependencyEntries.getModel(oe);
                if (dependency != null) {
                    dependenciesRootNode.add(new DubTreeNode(dependency));
                }
                return true;
            });
        }
        root.add(dependenciesRootNode);

        Tree<DubTreeNode> tree = Tree.create(new DubTreeModel(root));

        myRoot.center(ScrollableLayout.create(tree));
    }

    /**
     * Upstream's {@code DefaultMutableTreeNode} with its user object.
     */
    public static class DubTreeNode {
        private final Object myUserObject;
        private final List<DubTreeNode> myChildren = new ArrayList<>();

        public DubTreeNode(Object userObject) {
            myUserObject = userObject;
        }

        public Object getUserObject() {
            return myUserObject;
        }

        public List<DubTreeNode> getChildren() {
            return myChildren;
        }

        public void add(DubTreeNode child) {
            myChildren.add(child);
        }
    }

    public static class RunnerAndConfigurationSettingsNode extends DubTreeNode {
        private final RunnerAndConfigurationSettings mySettings;

        public RunnerAndConfigurationSettingsNode(RunnerAndConfigurationSettings settings) {
            super(settings);
            mySettings = settings;
        }

        public RunnerAndConfigurationSettings getSettings() {
            return mySettings;
        }
    }

    private enum DependenciesNode {
        INSTANCE
    }

    private static class DubTreeModel implements TreeModel<DubTreeNode> {
        private final DubTreeNode myRoot;

        DubTreeModel(DubTreeNode root) {
            myRoot = root;
        }

        @Override
        public void buildChildren(Function<DubTreeNode, TreeNode<DubTreeNode>> nodeFactory, @Nullable DubTreeNode parentValue) {
            List<DubTreeNode> children = parentValue == null ? List.of(myRoot) : parentValue.getChildren();
            for (DubTreeNode child : children) {
                TreeNode<DubTreeNode> node = nodeFactory.apply(child);
                node.setLeaf(child.getChildren().isEmpty());
                node.setRenderer(DubTreeRenderer::customizeCellRenderer);
            }
        }
    }

    public static class DubTreeRenderer {
        private DubTreeRenderer() {
        }

        public static void customizeCellRenderer(DubTreeNode node, TextItemPresentation presentation) {
            Object userObject = node.getUserObject();
            if (userObject instanceof Module module) {
                presentation.withIcon(DLanguage.Icons.LIBRARY);
                presentation.append(module.getName()); //, SimpleTextAttributes.STYLE_BOLD)
            }
            else if (userObject instanceof RunnerAndConfigurationSettings settings) {
                presentation.withIcon(PlatformIconGroup.runconfigurationsApplication());
                presentation.append(settings.getName());
            }
            else if (userObject instanceof DependenciesNode) {
                presentation.withIcon(PlatformIconGroup.nodesModulegroup());
                presentation.append(DLocalize.dubToolWindowDependencies()); //, SimpleTextAttributes.STYLE_BOLD)
            }
            else if (userObject instanceof DubDependencyOrderEntryModel dependency) {
                presentation.withIcon(PlatformIconGroup.nodesPplib());
                presentation.append(dependency.getName() + " " + dependency.getVersion(),
                    new TextAttribute(TextAttribute.STYLE_ITALIC, StandardColors.GRAY)
                );
            }
            else if (userObject instanceof LocalizeValue text) {
                presentation.append(text);
            }
            else {
                presentation.append(userObject.toString());
            }
        }
    }

    @RequiredUIAccess
    private Component createToolbarPanel() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new RefreshAction(this::createToolbarContent));
        //group.add(AddAction())
        group.addSeparator();
        group.add(new DubBuildAction()); // <-- defined in XML and code
        //group.add(ToggleOffline())
        group.addSeparator();
        group.add(new ConfigureDToolsAction());

        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("DubToolWindow", group, true);
        actionToolBar.setTargetUIComponent(myRoot);

        return actionToolBar.getUIComponent();
    }

    // the Disposable interface is so we can unregister any event listeners that get registered
    @Override
    public void dispose() {
        LOG.info("dispose of DubToolWindowPanel");
    }


    // -----

    public static class RefreshAction extends DumbAwareAction {
        private final @Nullable Runnable myCallback;

        public RefreshAction(@Nullable Runnable callback) {
            super(DLocalize.dubToolWindowRefresh(), LocalizeValue.empty(), PlatformIconGroup.actionsRefresh());
            myCallback = callback;
        }

        @RequiredUIAccess
        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = e.getData(Project.KEY);
            if (project == null) {
                return;
            }

            // Consulo: upstream chose the project SDK in a popup, then ran ProcessDLibs. The SDK belongs to the D
            // module extension and the DUB import resolves the dependencies, so this refreshes the linked DUB packages.
            ExternalSystemProjectRefresher.getInstance().refreshProjects(new ImportSpecBuilder(project, Dub.SYSTEM_ID).forceWhenUptodate());
            if (myCallback != null) {
                myCallback.run();
            }
        }
    }

    public static class AddAction extends DumbAwareAction {
        public AddAction() {
            super(DLocalize.dubToolWindowAdd(), LocalizeValue.empty(), PlatformIconGroup.generalAdd());
        }

        @RequiredUIAccess
        @Override
        public void actionPerformed(AnActionEvent e) {
            throw new UnsupportedOperationException("add a dub.json file");
        }
    }

    // set the '--nodeps'or maybe '--skip-registry=all' flag on dub args
    public static class ToggleOffline extends ToggleAction {
        public ToggleOffline() {
            super(DLocalize.dubToolWindowToggleOffline(), DLocalize.dubToolWindowToggleOfflineDescription(), PlatformIconGroup.nodesExceptionclass());
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return false; // todo:
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            // todo
        }
    }

}

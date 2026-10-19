package consulo.d.dub.project;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.dub.DubConstants;
import consulo.d.module.extension.DMutableModuleExtension;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtension;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;

import java.util.Collection;

/**
 * Turns D on for an imported module and writes what DUB reported it compiles with into the D module extension, where
 * the module-aware index reads it.
 */
@ExtensionImpl
public class DubModuleDataService implements ProjectDataService<DubModuleData, Module> {
    @Override
    public Key<DubModuleData> getTargetDataKey() {
        return DubModuleData.KEY;
    }

    @Override
    public void importData(Collection<DataNode<DubModuleData>> toImport, Project project, boolean synchronous) {
        if (toImport.isEmpty()) {
            return;
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (DataNode<DubModuleData> node : toImport) {
                    DataNode<ModuleData> moduleNode = ExternalSystemApiUtil.findParent(node, ProjectKeys.MODULE);
                    Module module = moduleNode == null ? null : moduleManager.findModuleByName(moduleNode.getData().getInternalName());
                    if (module != null) {
                        enableD(module, node.getData());
                    }
                }
            }
        });
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }

    private static void enableD(Module module, DubModuleData data) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        DMutableModuleExtension extension = rootModel.getExtensionWithoutCheck(DMutableModuleExtension.class);
        if (extension == null) {
            rootModel.dispose();
            return;
        }
        extension.setEnabled(true);
        extension.setVersionIdentifiers(data.getVersionIdentifiers());
        extension.setDebugIdentifiers(data.getDebugIdentifiers());
        if (rootModel.findModuleExtensionSdkEntry(extension) == null) {
            rootModel.addModuleExtensionSdkEntry(extension);
        }
        ExternalSystemMutableModuleExtension<?> dubExtension = rootModel.getExtensionWithoutCheck(DubConstants.SYSTEM_ID.getId());
        if (dubExtension != null) {
            dubExtension.setOption(DubConstants.TARGET_EXECUTABLE_OPTION, data.getTargetExecutable());
        }
        if (rootModel.isChanged()) {
            rootModel.commit();
        }
        else {
            rootModel.dispose();
        }
    }
}

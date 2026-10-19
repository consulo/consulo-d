package consulo.d.dub.project;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.dub.orderEntry.DubDependencyEntries;
import consulo.d.dub.orderEntry.DubDependencyOrderEntryModel;
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
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ExtensionImpl
public class DubDependencyDataService implements ProjectDataService<DubDependencyData, Module> {
    @Override
    public Key<DubDependencyData> getTargetDataKey() {
        return DubDependencyData.KEY;
    }

    @Override
    @RequiredUIAccess
    public void importData(Collection<DataNode<DubDependencyData>> toImport, Project project, boolean synchronous) {
        Map<String, List<DubDependencyOrderEntryModel>> byModule = new LinkedHashMap<>();
        List<Path> directories = new ArrayList<>();
        for (DataNode<DubDependencyData> node : toImport) {
            ModuleData moduleData = node.getData(ProjectKeys.MODULE);
            if (moduleData == null) {
                continue;
            }
            DubDependencyData data = node.getData();
            directories.add(Path.of(data.getDirectory()));
            List<String> sourceRootUrls = new ArrayList<>();
            for (String importPath : data.getImportPaths()) {
                sourceRootUrls.add(VirtualFileUtil.pathToUrl(importPath));
            }
            byModule.computeIfAbsent(moduleData.getInternalName(), name -> new ArrayList<>()).add(new DubDependencyOrderEntryModel(
                data.getName(),
                data.getVersion(),
                VirtualFileUtil.pathToUrl(data.getDirectory()),
                sourceRootUrls,
                data.getVersionIdentifiers(),
                data.getDebugIdentifiers()));
        }
        if (byModule.isEmpty()) {
            return;
        }

        // freshly fetched packages are not in the VFS yet; their roots must resolve before the root models change
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        for (Path directory : directories) {
            fileSystem.refreshAndFindFileByNioFile(directory);
        }

        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            @RequiredUIAccess
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (Map.Entry<String, List<DubDependencyOrderEntryModel>> entry : byModule.entrySet()) {
                    Module module = moduleManager.findModuleByName(entry.getKey());
                    if (module == null || module.isDisposed()) {
                        continue;
                    }
                    ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
                    DubDependencyEntries.replace(rootModel, entry.getValue());
                    if (rootModel.isChanged()) {
                        rootModel.commit();
                    }
                    else {
                        rootModel.dispose();
                    }
                }
            }
        });
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}

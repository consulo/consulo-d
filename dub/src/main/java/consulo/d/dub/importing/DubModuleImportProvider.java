package consulo.d.dub.importing;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.dub.DubConstants;
import consulo.externalSystem.importing.AbstractExternalModuleImportProvider;
import consulo.externalSystem.importing.ExternalModuleImportContext;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.d.icon.DIconGroup;
import jakarta.inject.Inject;

import java.io.File;

/**
 * Opens/imports a directory holding {@code dub.json} or {@code dub.sdl} (or the manifest itself). The package directory
 * is the linked project path.
 */
@ExtensionImpl
public class DubModuleImportProvider extends AbstractExternalModuleImportProvider {
    @Inject
    public DubModuleImportProvider(ProjectDataManager dataManager) {
        super(dataManager, DubConstants.SYSTEM_ID);
    }

    @Override
    public Image getIcon() {
        return DIconGroup.dubBall();
    }

    @Override
    public boolean canImport(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (String manifest : DubConstants.MANIFESTS) {
                if (new File(fileOrDirectory, manifest).isFile()) {
                    return true;
                }
            }
            return false;
        }
        return DubConstants.MANIFESTS.contains(fileOrDirectory.getName());
    }

    @Override
    protected File getExternalProjectConfigToUse(File file) {
        return file.isDirectory() ? file : file.getParentFile();
    }

    @Override
    protected void doPrepare(ExternalModuleImportContext context) {
    }

    @Override
    protected void beforeCommit(DataNode<ProjectData> dataNode, Project project) {
    }

    @Override
    protected void applyExtraSettings(ExternalModuleImportContext context) {
    }
}

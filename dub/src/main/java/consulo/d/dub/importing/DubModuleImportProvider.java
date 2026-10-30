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
import java.nio.file.Files;
import java.nio.file.Path;

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
    public boolean canImport(Path fileOrDirectory) {
        if (Files.isDirectory(fileOrDirectory)) {
            for (String manifest : DubConstants.MANIFESTS) {
                if (Files.isRegularFile(fileOrDirectory.resolve(manifest))) {
                    return true;
                }
            }
            return false;
        }
        Path fileName = fileOrDirectory.getFileName();
        return fileName != null && DubConstants.MANIFESTS.contains(fileName.toString());
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

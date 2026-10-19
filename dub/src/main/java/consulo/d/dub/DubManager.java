package consulo.d.dub;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.dub.project.DubProjectResolver;
import consulo.d.dub.setting.DubExecutionSettings;
import consulo.d.dub.setting.DubLocalSettings;
import consulo.d.dub.setting.DubProjectSettings;
import consulo.d.dub.setting.DubSettings;
import consulo.d.dub.setting.DubSettingsListener;
import consulo.d.dub.task.DubTaskManager;
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@ExtensionImpl
public class DubManager implements ExternalSystemAutoImportAware,
    ExternalSystemManager<DubProjectSettings, DubSettingsListener, DubSettings, DubLocalSettings, DubExecutionSettings> {

    private final DubAutoImportAware myAutoImportAware = new DubAutoImportAware();
    private final ExternalSystemAutoImportAware myAutoImportDelegate = new CachingExternalSystemAutoImportAware(myAutoImportAware);

    @Override
    public ProjectSystemId getSystemId() {
        return DubConstants.SYSTEM_ID;
    }

    @Override
    public Function<Project, DubSettings> getSettingsProvider() {
        return DubSettings::getInstance;
    }

    @Override
    public Function<Project, DubLocalSettings> getLocalSettingsProvider() {
        return DubLocalSettings::getInstance;
    }

    @Override
    public Function<Pair<Project, String>, DubExecutionSettings> getExecutionSettingsProvider() {
        return pair -> DubUtil.createExecutionSettings(pair.getFirst(), pair.getSecond());
    }

    @Override
    public Supplier<? extends ExternalSystemProjectResolver<DubExecutionSettings>> getProjectResolverFactory() {
        return DubProjectResolver::new;
    }

    @Override
    public Supplier<? extends ExternalSystemTaskManager<DubExecutionSettings>> getTaskManagerFactory() {
        return DubTaskManager::new;
    }

    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return file.isDirectory() || DubConstants.MANIFESTS.contains(file.getName());
            }
        };
    }

    @Override
    public @Nullable String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        return myAutoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        return myAutoImportAware.getAffectedExternalProjectFilePaths(projectPath, project);
    }
}

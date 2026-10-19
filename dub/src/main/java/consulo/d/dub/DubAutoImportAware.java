package consulo.d.dub;

import consulo.d.dub.setting.DubProjectSettings;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DubAutoImportAware implements ExternalSystemAutoImportAware {
    @Override
    public @Nullable String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        Path changed = Path.of(changedFileOrDirPath);
        Path fileName = changed.getFileName();
        if (fileName == null) {
            return null;
        }
        String name = fileName.toString();
        if (!DubConstants.MANIFESTS.contains(name) && !DubConstants.SELECTIONS.equals(name)) {
            return null;
        }
        Path directory = changed.getParent();
        if (directory == null) {
            return null;
        }
        DubProjectSettings settings = DubUtil.findProjectSettings(project, directory.toString());
        return settings == null ? null : settings.getExternalProjectPath();
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        Path directory = DubUtil.toPackageDirectory(Path.of(projectPath));
        List<Path> paths = new ArrayList<>();
        for (String manifest : DubConstants.MANIFESTS) {
            paths.add(directory.resolve(manifest));
        }
        paths.add(directory.resolve(DubConstants.SELECTIONS));
        return paths;
    }
}

package consulo.d.dub;

import consulo.d.dub.setting.DubExecutionSettings;
import consulo.d.dub.setting.DubProjectSettings;
import consulo.d.dub.setting.DubSettings;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtension;
import consulo.externalSystem.util.ExternalSystemConstants;
import consulo.module.Module;
import consulo.project.Project;
import io.github.intellij.dub.service.DubBinaryPathProvider;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DubUtil {
    private DubUtil() {
    }

    public static DubExecutionSettings createExecutionSettings(Project project, String linkedProjectPath) {
        DubProjectSettings settings = findProjectSettings(project, linkedProjectPath);
        String executable = settings == null ? "" : settings.getDubExecutable().trim();
        if (executable.isEmpty()) {
            String configured = DubBinaryPathProvider.INSTANCE.getDubPath();
            executable = configured != null ? configured : DubConstants.DEFAULT_EXECUTABLE;
        }
        return new DubExecutionSettings(executable);
    }

    /**
     * The settings of the linked DUB package whose directory is (or holds) {@code packagePath}.
     */
    public static @Nullable DubProjectSettings findProjectSettings(Project project, String packagePath) {
        Path packageDirectory = toPackageDirectory(Path.of(packagePath));
        for (DubProjectSettings settings : DubSettings.getInstance(project).getLinkedProjectsSettings()) {
            String linkedProjectPath = settings.getExternalProjectPath();
            if (linkedProjectPath != null && toPackageDirectory(Path.of(linkedProjectPath)).equals(packageDirectory)) {
                return settings;
            }
        }
        return null;
    }

    /**
     * A linked project is the package directory, or the {@code dub.json} / {@code dub.sdl} in it.
     */
    public static Path toPackageDirectory(Path fileOrDirectory) {
        if (Files.isDirectory(fileOrDirectory)) {
            return fileOrDirectory;
        }
        Path parent = fileOrDirectory.getParent();
        return parent == null ? fileOrDirectory : parent;
    }

    /**
     * @return the executable the root package of {@code module} builds to, as the last import found it
     */
    public static @Nullable Path findTargetExecutable(Module module) {
        ExternalSystemModuleExtension<?> extension = module.getExtension(ExternalSystemModuleExtension.class);
        if (extension == null || !DubConstants.SYSTEM_ID.getId().equals(extension.getOption(ExternalSystemConstants.EXTERNAL_SYSTEM_ID_KEY))) {
            return null;
        }
        String targetExecutable = extension.getOption(DubConstants.TARGET_EXECUTABLE_OPTION);
        return targetExecutable == null ? null : Path.of(targetExecutable);
    }

    /**
     * @return the directory of the DUB package {@code module} was imported from
     */
    public static @Nullable Path findPackageDirectory(Module module) {
        ExternalSystemModuleExtension<?> extension = module.getExtension(ExternalSystemModuleExtension.class);
        if (extension == null || !DubConstants.SYSTEM_ID.getId().equals(extension.getOption(ExternalSystemConstants.EXTERNAL_SYSTEM_ID_KEY))) {
            return null;
        }
        String linkedProjectPath = extension.getOption(ExternalSystemConstants.LINKED_PROJECT_PATH_KEY);
        return linkedProjectPath == null ? null : toPackageDirectory(Path.of(linkedProjectPath));
    }

    public static @Nullable Path findManifest(Path packageDirectory) {
        for (String manifest : DubConstants.MANIFESTS) {
            Path path = packageDirectory.resolve(manifest);
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }
}

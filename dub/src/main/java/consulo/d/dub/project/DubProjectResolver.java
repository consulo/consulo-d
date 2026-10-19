package consulo.d.dub.project;

import consulo.d.dub.DubConstants;
import consulo.d.dub.DubUtil;
import consulo.d.dub.setting.DubExecutionSettings;
import consulo.d.dub.task.DubCommand;
import consulo.d.localize.DLocalize;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.project.BuildSettings;
import io.github.intellij.dub.project.DubPackage;
import io.github.intellij.dub.project.DubProject;
import io.github.intellij.dub.project.DubTarget;
import io.github.intellij.dub.tools.DescribeParserException;
import io.github.intellij.dub.tools.DescribeParserImpl;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static consulo.d.dub.DubConstants.SYSTEM_ID;

public class DubProjectResolver implements ExternalSystemProjectResolver<DubExecutionSettings> {
    private static final Map<String, LocalizeValue> TASKS = new LinkedHashMap<>();

    static {
        TASKS.put(DubConstants.TASK_BUILD, DLocalize.dubTaskBuildDescription());
        TASKS.put(DubConstants.TASK_RUN, DLocalize.dubTaskRunDescription());
        TASKS.put(DubConstants.TASK_TEST, DLocalize.dubTaskTestDescription());
        TASKS.put(DubConstants.TASK_CLEAN, DLocalize.dubTaskCleanDescription());
        TASKS.put(DubConstants.TASK_UPGRADE, DLocalize.dubTaskUpgradeDescription());
        TASKS.put(DubConstants.TASK_FETCH, DLocalize.dubTaskFetchDescription());
    }

    private final DubCommand myCommand = new DubCommand();

    @Override
    public @Nullable DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                              String projectPath,
                                                              boolean isPreviewMode,
                                                              @Nullable DubExecutionSettings settings,
                                                              ExternalSystemTaskNotificationListener listener)
        throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        Path packageDirectory = DubUtil.toPackageDirectory(Path.of(projectPath));
        if (DubUtil.findManifest(packageDirectory) == null) {
            throw new IllegalArgumentException(DLocalize.dubErrorNoManifest(packageDirectory).get());
        }

        String executable = settings == null ? DubConstants.DEFAULT_EXECUTABLE : settings.getDubExecutable();
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, DLocalize.dubStatusReading().get()));
        String json = myCommand.run(id, executable, packageDirectory, List.of("describe"), false, listener);

        DubProject dubProject;
        try {
            dubProject = new DescribeParserImpl().parse(json);
        }
        catch (DescribeParserException e) {
            throw new ExternalSystemException(DLocalize.dubErrorBadOutput("dub describe", String.valueOf(e.getMessage())).get());
        }
        return createProjectNode(packageDirectory, projectPath, dubProject);
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId taskId, ExternalSystemTaskNotificationListener listener) {
        return myCommand.cancel();
    }

    public static DataNode<ProjectData> createProjectNode(Path packageDirectory, String linkedProjectPath, DubProject dubProject) {
        String directory = packageDirectory.toString();
        String name = dubProject.rootPackageName().isEmpty() ? String.valueOf(packageDirectory.getFileName()) : dubProject.rootPackageName();

        ProjectData projectData = new ProjectData(SYSTEM_ID, name, directory, linkedProjectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);
        for (Map.Entry<String, LocalizeValue> task : TASKS.entrySet()) {
            projectNode.createChild(ProjectKeys.TASK, new TaskData(SYSTEM_ID, task.getKey(), linkedProjectPath, task.getValue().get()));
        }

        ModuleData moduleData = new ModuleData(name, SYSTEM_ID, name, directory, linkedProjectPath);
        DataNode<ModuleData> moduleNode = projectNode.createChild(ProjectKeys.MODULE, moduleData);

        ContentRootData contentRoot = new ContentRootData(SYSTEM_ID, directory);
        for (Path sourceRoot : sourceRoots(packageDirectory, dubProject.rootPackage())) {
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, sourceRoot.toString());
        }
        contentRoot.storePath(ExternalSystemSourceType.EXCLUDED, packageDirectory.resolve(DubConstants.DUB_DIRECTORY).toString());
        moduleNode.createChild(ProjectKeys.CONTENT_ROOT, contentRoot);

        moduleNode.createChild(DubModuleData.KEY, new DubModuleData(
            versionIdentifiers(dubProject),
            debugIdentifiers(dubProject),
            targetExecutable(packageDirectory, dubProject.rootPackage())
        ));

        for (DubDependency dependency : dependencies(packageDirectory, dubProject)) {
            moduleNode.createChild(DubDependencyData.KEY, new DubDependencyData(
                dependency.name(),
                dependency.version(),
                dependency.directory(),
                dependency.importPaths(),
                dependency.versionIdentifiers(),
                dependency.debugIdentifiers()
            ));
        }
        return projectNode;
    }


    /**
     * One package of the dependency tree, with its import paths made absolute.
     */
    public record DubDependency(String name,
                                String version,
                                String directory,
                                List<String> importPaths,
                                List<String> versionIdentifiers,
                                List<String> debugIdentifiers) {
    }

    /**
     * What the module compiles with: the identifiers the platform, architecture and compiler predefine, then the
     * {@code versions} of the root package's target.
     */
    public static List<String> versionIdentifiers(DubProject dubProject) {
        return withPredefined(DubPredefinedVersions.of(dubProject), versions(dubProject, dubProject.rootPackage()));
    }

    /**
     * The {@code debug} identifiers the root package compiles with.
     */
    public static List<String> debugIdentifiers(DubProject dubProject) {
        return debugVersions(dubProject, dubProject.rootPackage());
    }

    /**
     * The file the package builds to - {@code targetPath} / {@code targetFileName} of {@code dub describe} - or
     * {@code null} when it builds none.
     */
    public static @Nullable String targetExecutable(Path packageDirectory, DubPackage rootPackage) {
        if (rootPackage.targetFileName().isEmpty()) {
            return null;
        }
        return packagePath(rootPackage, packageDirectory).resolve(rootPackage.targetPath()).resolve(rootPackage.targetFileName()).normalize().toString();
    }

    /**
     * The import paths of the root package that lie inside the package directory.
     */
    public static List<Path> sourceRoots(Path packageDirectory, DubPackage rootPackage) {
        Path rootPackagePath = packagePath(rootPackage, packageDirectory);
        List<Path> sourceRoots = new ArrayList<>();
        for (String importPath : rootPackage.sourcesDirs()) {
            Path sourceRoot = rootPackagePath.resolve(importPath).normalize();
            if (sourceRoot.startsWith(packageDirectory)) {
                sourceRoots.add(sourceRoot);
            }
        }
        return sourceRoots;
    }

    /**
     * Every package is compiled by the same compiler for the same platform, so each one's identifiers are the
     * predefined ones followed by the {@code versions} of the target that compiles it.
     */
    public static List<DubDependency> dependencies(Path packageDirectory, DubProject dubProject) {
        List<String> predefined = DubPredefinedVersions.of(dubProject);
        List<DubDependency> dependencies = new ArrayList<>();
        for (DubPackage dependency : dubProject.packages()) {
            List<String> versionIdentifiers = withPredefined(predefined, versions(dubProject, dependency));
            Path dependencyPath = packagePath(dependency, packageDirectory);
            List<String> importPaths = new ArrayList<>();
            for (String importPath : dependency.sourcesDirs()) {
                importPaths.add(dependencyPath.resolve(importPath).normalize().toString());
            }
            dependencies.add(new DubDependency(
                dependency.name(),
                dependency.version(),
                dependencyPath.toString(),
                importPaths,
                versionIdentifiers,
                debugVersions(dubProject, dependency)
            ));
        }
        return dependencies;
    }

    /**
     * The {@code versions} a package is compiled with in this build: those of the target that compiles it - DUB adds
     * the versions of the dependencies and of the root package, and a {@code Have_<package>} for each package of the
     * target - or, when no target covers it, the package's own.
     */
    private static List<String> versions(DubProject dubProject, DubPackage dubPackage) {
        BuildSettings buildSettings = buildSettings(dubProject, dubPackage.name());
        return buildSettings != null ? buildSettings.versions() : dubPackage.versions();
    }

    private static List<String> debugVersions(DubProject dubProject, DubPackage dubPackage) {
        BuildSettings buildSettings = buildSettings(dubProject, dubPackage.name());
        return buildSettings != null ? buildSettings.debugVersions() : dubPackage.debugVersions();
    }

    /**
     * The build settings of the target built from the package, else of the first target the package is compiled into
     * (a {@code sourceLibrary} has no target of its own).
     */
    private static @Nullable BuildSettings buildSettings(DubProject dubProject, String packageName) {
        DubTarget containing = null;
        for (DubTarget target : dubProject.targets()) {
            if (target.buildSettings() == null) {
                continue;
            }
            if (target.rootPackage().equals(packageName)) {
                return target.buildSettings();
            }
            if (containing == null && target.packages().contains(packageName)) {
                containing = target;
            }
        }
        return containing != null ? containing.buildSettings() : null;
    }

    private static List<String> withPredefined(List<String> predefined, List<String> versions) {
        List<String> versionIdentifiers = new ArrayList<>(predefined);
        for (String version : versions) {
            if (!versionIdentifiers.contains(version)) {
                versionIdentifiers.add(version);
            }
        }
        return versionIdentifiers;
    }

    private static Path packagePath(DubPackage dubPackage, Path fallback) {
        return dubPackage.path().isEmpty() ? fallback : Path.of(dubPackage.path()).normalize();
    }
}

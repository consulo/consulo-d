package io.github.intellij.dub.project;

import io.github.intellij.dlanguage.project.Dependency;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * moved over from Java code Feb 2018. Class is essentially the same but now implements Dependency
 */
public record DubPackage(
    String name,
    String path,
    String version,
    //val targetType: String,
    String targetPath,
    //val targetName: String,
    String targetFileName,
    // the following can potentially be empty
    String description,
    String homepage,
    List<String> authors,
    String copyright,
    String license,
    List<String> dependencies,
    //val active: Boolean,
    //val configuration: String,
    String workingDirectory,
    String mainSourceFile,
    //val dflags: List<String> = emptyList(),
    //val lflags: List<String> = emptyList(),
    List<String> libs,
    List<String> copyFiles,
    List<String> extraDependencyFiles,
    List<String> versions,
    List<String> debugVersions,
//    val importPaths: List<String> = emptyList(),
//    val stringImportPaths: List<String> = emptyList(),
    List<String> sourcesDirs, // importPaths
    List<String> resources, // stringImportPaths
    List<String> sourceFiles,
    List<String> stringImportFiles,
    List<String> preGenerateCommands,
    List<String> postGenerateCommands,
    List<String> preBuildCommands,
    List<String> postBuildCommands,
    List<String> preRunCommands,
    List<String> postRunCommands,
    Map<String, String> environments,
    Map<String, String> buildEnvironments,
    Map<String, String> runEnvironments,
    Map<String, String> preGenerateEnvironments,
    Map<String, String> postGenerateEnvironments,
    Map<String, String> preBuildEnvironments,
    Map<String, String> postBuildEnvironments,
    Map<String, String> preRunEnvironments,
    @Nullable Map<String, String> postRunEnvironments,
    List<String> options,
    List<DubPackageFile> files
) implements Dependency {
    @Override
    public String getDisplayName() {
        return name + "-" + version;
    }
}

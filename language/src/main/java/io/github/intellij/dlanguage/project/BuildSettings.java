package io.github.intellij.dlanguage.project;

import java.util.List;

public record BuildSettings(
    int targetType,
    String targetPath,
    String targetName,
    String workingDirectory,
    String mainSourceFile,
    List<String> dflags,
    List<String> lflags,
    List<String> libs,
    List<String> linkerFiles,
    List<String> sourceFiles,
    List<String> copyFiles,
    List<String> extraDependencyFiles,
    List<String> versions,
    List<String> debugVersions,
    List<String> versionFilters,
    List<String> debugVersionFilters,
    List<String> importPaths,
    List<String> stringImportPaths,
    List<String> importFiles,
    List<String> stringImportFiles,
    List<String> preGenerateCommands,
    List<String> postGenerateCommands,
    List<String> preBuildCommands,
    List<String> postBuildCommands,
    List<String> preRunCommands,
    List<String> postRunCommands
) {
}

package io.github.intellij.dub.project;

public record DubPackageFile(
    String role, // eg: "source", "unusedSource", "stringImport", or "import_"
    String path  // the relative path from package root
) {
}

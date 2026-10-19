package io.github.intellij.dlanguage.run.exception;

import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

public class NoSourcesException extends Exception {
    private final VirtualFile sourcesRoot;

    public NoSourcesException(final VirtualFile sourcesRoot) {
        super("No D sources found in " + sourcesRoot.getCanonicalPath());
        this.sourcesRoot = sourcesRoot;
    }

    public @Nullable String getSourcesRoot() {
        return sourcesRoot.getCanonicalPath();
    }
}

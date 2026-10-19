package io.github.intellij.dub.service;

import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.settings.ToolKey;
import io.github.intellij.dlanguage.tools.DtoolUtils;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class DubBinaryPathProvider {
    public static final DubBinaryPathProvider INSTANCE = new DubBinaryPathProvider();

    private DubBinaryPathProvider() {
    }

    /**
     * Return the path on the Dub binary or null if the dub is unavailable (or missconfigured)
     */
    public @Nullable String getDubPath() {
        String path = ToolKey.DUB_KEY.getPath();
        if (path != null) {
            return path;
        }

        return getAutodetectPath();
    }

    public @Nullable String getAutodetectPath() {
        return DtoolUtils.INSTANCE.lookInStandardDirectories(ToolKey.DUB_KEY.getName());
    }

    public boolean isDubAvailable() {
        String dubBinaryPath = getDubPath();
        return dubBinaryPath != null && verifyDubBinary(dubBinaryPath);
    }

    public boolean verifyDubBinary(String path) {
        if (!Files.isExecutable(Paths.get(path)) || Files.isDirectory(Paths.get(path))) {
            return false;
        }
        String output = getVersionOutput(path);
        return StringUtil.isNotEmpty(output) && output.startsWith("DUB");
    }

    public String getVersionOutput(String path) {
        return DtoolUtils.getVersionOutput(path);
    }
}

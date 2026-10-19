package io.github.intellij.dlanguage.settings;

import consulo.application.ApplicationPropertiesComponent;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

/**
 * String wrapper to ensure that we don't accidentally pass an invalid key to the PropertiesComponent.
 * These are set as property keys in DToolsConfigurable.
 */
public class ToolKey {
    public static final ToolKey DUB_KEY = new ToolKey("dub");
    public static final ToolKey DSCANNER_KEY = new ToolKey("dscanner");
    public static final ToolKey DCD_SERVER_KEY = new ToolKey("dcd-server");
    public static final ToolKey DCD_CLIENT_KEY = new ToolKey("dcd-client");
    public static final ToolKey DFORMAT_KEY = new ToolKey("dfmt");
    public static final ToolKey GDB_KEY = new ToolKey("gdb");

    private final String name;
    private final String pathKey;
    private final String flagsKey;

    private ToolKey(final String name) {
        this.name = name;
        this.pathKey = name + "Path";
        this.flagsKey = name + "Flags";
    }

    public String getName() {
        return name;
    }

    public String getPathKey() {
        return pathKey;
    }

    public String getFlagsKey() {
        return flagsKey;
    }

    public @Nullable String getPath() {
        final String path = ApplicationPropertiesComponent.getInstance().getValue(pathKey);
        return path == null || path.isEmpty() ? null : StringUtil.trim(path);
    }

    public void setPath(@Nullable final String newValue) {
        ApplicationPropertiesComponent.getInstance().setValue(pathKey, StringUtil.trim(newValue));
    }

    public String getFlags() {
        final String flags = ApplicationPropertiesComponent.getInstance().getValue(flagsKey);
        return flags == null ? "" : StringUtil.trim(flags);
    }
}


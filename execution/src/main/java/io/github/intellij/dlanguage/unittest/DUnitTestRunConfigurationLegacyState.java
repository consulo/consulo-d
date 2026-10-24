package io.github.intellij.dlanguage.unittest;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The options a {@link DUnitTestRunConfiguration} wrote before the fragment model.
 */
public class DUnitTestRunConfigurationLegacyState {
    public @Nullable String dFilePath;
    public @Nullable String workingDir;
    public Map<String, String> envVars = new LinkedHashMap<>();
}

package io.github.intellij.dlanguage.messagebus;

import io.github.intellij.dlanguage.settings.ToolSettings;

/**
 * Port of upstream {@code listeners.kt}. Consulo identifies a topic by its listener interface, so each tool has its
 * own sub-interface (see {@link Topics}).
 */
public interface ToolChangeListener {
    /**
     * @param settings the ToolSettings (path and flags) for the D Tool that has been changed
     */
    void onToolSettingsChanged(ToolSettings settings);
}

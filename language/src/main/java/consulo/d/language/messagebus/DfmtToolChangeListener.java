package consulo.d.language.messagebus;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import io.github.intellij.dlanguage.messagebus.ToolChangeListener;
import io.github.intellij.dlanguage.settings.ToolSettings;

/**
 * The topic upstream created as {@code Topic.create("dfmt tool change", ToolChangeListener)}.
 * The method is declared again because Consulo's topic binding only sees the methods of the topic interface itself.
 */
@TopicAPI(ComponentScope.PROJECT)
public interface DfmtToolChangeListener extends ToolChangeListener {
    @Override
    void onToolSettingsChanged(ToolSettings settings);
}

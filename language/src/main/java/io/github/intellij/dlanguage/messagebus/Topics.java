package io.github.intellij.dlanguage.messagebus;

import consulo.d.language.messagebus.DScannerToolChangeListener;
import consulo.d.language.messagebus.DfmtToolChangeListener;
import consulo.d.language.messagebus.DubToolChangeListener;

/**
 * @author Samael Bate (singingbush)
 * Created on 03/02/2018.
 *
 * @since v1.16
 * All the potential Topics that we intend to use should be placed here for convenience
 * <p>
 * Consulo identifies a topic by its {@code @TopicAPI} listener interface, so the topics are those classes. The DCD
 * and GDB topics are not ported: DCD is not supported and debugging comes from the nativeDev plugin.
 */
public final class Topics {
    // a change to the dub.json or dub.sdl file
    public static final Class<DubChangeNotifier> DUB_FILE_CHANGE = DubChangeNotifier.class;

    // ToolChangeListeners are for when the path or args for the various D Tools are changed
    public static final Class<DubToolChangeListener> DUB_TOOL_CHANGE = DubToolChangeListener.class;
    public static final Class<DScannerToolChangeListener> DSCANNER_TOOL_CHANGE = DScannerToolChangeListener.class;
    public static final Class<DfmtToolChangeListener> DFMT_TOOL_CHANGE = DfmtToolChangeListener.class;

    private Topics() {
    }
}

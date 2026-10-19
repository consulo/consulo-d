package consulo.d.execution.debugger;

import consulo.annotation.component.ExtensionImpl;
import consulo.nativeDev.debugger.NativeLineBreakpointTypeResolver;
import consulo.virtualFileSystem.fileType.FileType;
import io.github.intellij.dlanguage.DlangFileType;

/**
 * Line breakpoints in D files, for every native debugger.
 */
@ExtensionImpl
public class DLineBreakpointTypeResolver extends NativeLineBreakpointTypeResolver {
    @Override
    public FileType getFileType() {
        return DlangFileType.INSTANCE;
    }
}

package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import io.github.intellij.dlanguage.DlangFileType;

/**
 * Registers {@link DlangFileType}; upstream did it in {@code plugin.xml} ({@code <fileType extensions="d;di"/>}).
 */
@ExtensionImpl
public class DlangFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(DlangFileType.INSTANCE, "d;di");
    }
}

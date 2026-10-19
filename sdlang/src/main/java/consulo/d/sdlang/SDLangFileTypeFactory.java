package consulo.d.sdlang;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import io.github.intellij.dlanguage.sdlang.SDLangFileType;

/**
 * Registers {@link SDLangFileType}; upstream did it in {@code sdlang.xml} ({@code <fileType extensions="sdl"/>}).
 */
@ExtensionImpl
public class SDLangFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(SDLangFileType.INSTANCE, "sdl");
    }
}

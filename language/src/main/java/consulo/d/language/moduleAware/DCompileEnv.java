package consulo.d.language.moduleAware;

import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.ModuleAwareIndexOptions;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Reads back the options the module-aware index recorded for a file, so stub building, the editor and highlighting
 * all see the variant the file was indexed under.
 */
public final class DCompileEnv {
    public static final String PROVIDER_ID = "d-module-aware";

    private DCompileEnv() {
    }

    /**
     * @return {@code null} when nothing is known about how the file is compiled, in which case every branch counts as
     * compiled
     */
    public static @Nullable DCompileOptions optionsFor(PsiElement context) {
        return read(() -> ModuleAwareIndexOptions.getOptions(context, PROVIDER_ID, DCompileOptionsExternalizer.INSTANCE));
    }

    public static @Nullable DCompileOptions optionsFor(Project project, VirtualFile file) {
        return read(() -> ModuleAwareIndexOptions.getOptions(project, file, PROVIDER_ID, DCompileOptionsExternalizer.INSTANCE));
    }

    /**
     * The module-aware index is a service of a full application; a light fixture which only parses a file has no such
     * service, and there everything counts as compiled.
     */
    private static @Nullable DCompileOptions read(Supplier<@Nullable DCompileOptions> reader) {
        try {
            return reader.get();
        }
        catch (IllegalArgumentException serviceNotBound) {
            return null;
        }
    }
}

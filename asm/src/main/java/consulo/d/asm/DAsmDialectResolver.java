package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.asm.language.AsmDefaultVersionResolver;
import consulo.d.module.extension.DModuleExtension;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * The assembly files of a D module ({@code .s}, and {@code .S} and {@code .sx} which are preprocessed first) are
 * assembled by the GNU assembler of the target the D compiler builds for.
 */
@ExtensionImpl
public class DAsmDialectResolver implements AsmDefaultVersionResolver {
    private static final Set<String> EXTENSIONS = Set.of("s", "S", "sx");

    private final Project myProject;

    @Inject
    public DAsmDialectResolver(Project project) {
        myProject = project;
    }

    @Override
    public @Nullable LanguageVersion tryResolveDefaultVersion(VirtualFile file) {
        if (!EXTENSIONS.contains(file.getExtension())) {
            return null;
        }
        return ReadAction.compute(() -> resolve(file));
    }

    @RequiredReadAction
    private @Nullable LanguageVersion resolve(VirtualFile file) {
        DModuleExtension extension = DAsmDialects.findDModuleExtension(myProject, file);
        return extension == null ? null : DAsmDialects.forVersionIdentifiers(extension.getVersionIdentifiers());
    }
}

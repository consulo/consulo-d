package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.asm.gas.GasAarch64LanguageVersion;
import consulo.asm.gas.GasArmLanguageVersion;
import consulo.asm.gas.GasAttLanguageVersion;
import consulo.asm.gas.GasMipsLanguageVersion;
import consulo.asm.gas.GasRiscVLanguageVersion;
import consulo.asm.language.AsmLanguage;
import consulo.d.module.extension.DModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionUtil;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * The GNU as dialect D code is assembled with: GDC and LDC use the GNU assembler syntax of the target, which the
 * predefined version identifiers of the D module name ({@code X86_64}, {@code AArch64}, ...).
 */
public final class DAsmDialects {
    private DAsmDialects() {
    }

    /**
     * The D module {@code file} belongs to: its module, or for a file of a library (a DUB package, the D runtime) a
     * module using it.
     */
    @RequiredReadAction
    public static @Nullable DModuleExtension findDModuleExtension(Project project, VirtualFile file) {
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        Module module = fileIndex.getModuleForFile(file);
        if (module != null) {
            return ModuleUtilCore.getExtension(module, DModuleExtension.class);
        }
        for (OrderEntry entry : fileIndex.getOrderEntriesForFile(file)) {
            DModuleExtension extension = ModuleUtilCore.getExtension(entry.getOwnerModule(), DModuleExtension.class);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }

    public static LanguageVersion forVersionIdentifiers(Collection<String> versions) {
        Class<? extends LanguageVersion> versionClass = GasAttLanguageVersion.class;
        if (versions.contains("AArch64")) {
            versionClass = GasAarch64LanguageVersion.class;
        }
        else if (versions.contains("ARM")) {
            versionClass = GasArmLanguageVersion.class;
        }
        else if (versions.contains("RISCV32") || versions.contains("RISCV64")) {
            versionClass = GasRiscVLanguageVersion.class;
        }
        else if (versions.contains("MIPS32") || versions.contains("MIPS64")) {
            versionClass = GasMipsLanguageVersion.class;
        }
        LanguageVersion version = AsmLanguage.INSTANCE.findVersionByClass(versionClass);
        return version != null ? version : LanguageVersionUtil.findDefaultVersion(AsmLanguage.INSTANCE);
    }

    @RequiredReadAction
    public static LanguageVersion forFile(Project project, @Nullable VirtualFile file) {
        DModuleExtension extension = file == null ? null : findDModuleExtension(project, file);
        return forVersionIdentifiers(extension != null ? extension.getVersionIdentifiers() : List.of());
    }
}

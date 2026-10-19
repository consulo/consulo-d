package consulo.d.language.moduleAware;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.module.extension.DModuleExtension;
import consulo.language.psi.stub.IndexOption;
import consulo.language.psi.stub.ModuleAwareIndexOptionProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.OrderEnumerator;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import io.github.intellij.dlanguage.DlangFileType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tells the module-aware index that what a D file declares depends on the {@code version} / {@code debug} identifiers
 * of the module it is compiled in: modules with the same identifiers share one copy of the index, modules with
 * different ones get their own.
 * <p>
 * Files of no module - the SDK's druntime and phobos, libraries - are compiled as part of every module that depends on
 * them, so {@link #analyze} gives them one variant per distinct set of identifiers of those modules; an order entry
 * that knows how its files are compiled ({@link DCompileOptionsOwner}, a DUB package) answers for itself.
 */
@ExtensionImpl
public class DModuleAwareIndexOptionProvider implements ModuleAwareIndexOptionProvider {
    private static final Set<FileType> FILE_TYPES = Set.of(DlangFileType.INSTANCE);

    @Override
    public String getId() {
        return DCompileEnv.PROVIDER_ID;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public Set<FileType> getInputFileTypes() {
        return FILE_TYPES;
    }

    @Override
    public IndexOption getOptions(Module module, VirtualFile file) {
        DCompileOptions options = optionsOf(module);
        return options == null ? IndexOption.fullySharable() : toIndexOption(options);
    }

    @Override
    public Map<VirtualFile, List<IndexOption>> analyze(Project project, @Nullable Collection<VirtualFile> changedFiles) {
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        Collection<VirtualFile> files = changedFiles != null ? changedFiles : collectLibrarySources(project, fileIndex);

        Map<Module, @Nullable DCompileOptions> optionsByModule = new HashMap<>();
        Map<VirtualFile, List<IndexOption>> result = new HashMap<>();
        for (VirtualFile file : files) {
            if (!file.isValid() || file.isDirectory() || file.getFileType() != DlangFileType.INSTANCE) {
                continue;
            }
            if (fileIndex.getModuleForFile(file) != null) {
                continue;
            }

            Map<DCompileOptions, IndexOption> variants = new LinkedHashMap<>();
            for (OrderEntry entry : fileIndex.getOrderEntriesForFile(file)) {
                DCompileOptions options = entryOptions(entry);
                if (options == null) {
                    options = optionsByModule.computeIfAbsent(entry.getOwnerModule(), DModuleAwareIndexOptionProvider::optionsOf);
                }
                if (options != null && !variants.containsKey(options)) {
                    variants.put(options, toIndexOption(options));
                }
            }
            if (!variants.isEmpty()) {
                result.put(file, new ArrayList<>(variants.values()));
            }
        }
        return result;
    }

    private static @Nullable DCompileOptions entryOptions(OrderEntry entry) {
        if (entry instanceof CustomOrderEntry<?> customOrderEntry && customOrderEntry.getModel() instanceof DCompileOptionsOwner owner) {
            DCompileOptions options = owner.getCompileOptions();
            return options == null || options.isEmpty() ? null : options;
        }
        return null;
    }

    private static @Nullable DCompileOptions optionsOf(Module module) {
        DModuleExtension extension = ModuleUtilCore.getExtension(module, DModuleExtension.class);
        if (extension == null) {
            return null;
        }
        DCompileOptions options = new DCompileOptions(extension.getVersionIdentifiers(), extension.getDebugIdentifiers());
        return options.isEmpty() ? null : options;
    }

    private static IndexOption toIndexOption(DCompileOptions options) {
        return IndexOption.sharablePerOption(options, DCompileOptionsExternalizer.INSTANCE, LocalizeValue.of(options.format()));
    }

    private static Collection<VirtualFile> collectLibrarySources(Project project, ProjectFileIndex fileIndex) {
        Set<VirtualFile> roots = new LinkedHashSet<>();
        for (Module module : ModuleManager.getInstance(project).getSortedModules()) {
            if (optionsOf(module) == null) {
                continue;
            }
            for (VirtualFile root : OrderEnumerator.orderEntries(module).withoutModuleSourceEntries().sources().getRoots()) {
                roots.add(root);
            }
        }

        Set<VirtualFile> files = new LinkedHashSet<>();
        for (VirtualFile root : roots) {
            VirtualFileUtil.iterateChildrenRecursively(root, file -> !fileIndex.isExcluded(file), file -> {
                if (!file.isDirectory() && file.getFileType() == DlangFileType.INSTANCE && fileIndex.getModuleForFile(file) == null) {
                    files.add(file);
                }
                return true;
            });
        }
        return files;
    }
}

package io.github.intellij.dlanguage.index;

import consulo.annotation.component.ExtensionImpl;
import consulo.index.io.DataIndexer;
import consulo.index.io.EnumeratorStringDescriptor;
import consulo.index.io.ID;
import consulo.index.io.KeyDescriptor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.FileContent;
import consulo.language.psi.stub.ScalarIndexExtension;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This index keep a track of all modules names and allow to find the corresponding file.
 */
@ExtensionImpl
public class DModuleIndex extends ScalarIndexExtension<String> {

    public static final FileBasedIndex.InputFilter D_MODULE_FILTER = (project, file) -> file.getFileType() == DlangFileType.INSTANCE;
    private static final ID<String, Void> D_MODULE_INDEX = ID.create("DModuleIndex");
    private static final int INDEX_VERSION = 1;
    private static final EnumeratorStringDescriptor KEY_DESCRIPTOR = new EnumeratorStringDescriptor();
    private static final MyDataIndexer INDEXER = new MyDataIndexer();

    @Override
    public ID<String, Void> getName() {
        return D_MODULE_INDEX;
    }

    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return INDEXER;
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return KEY_DESCRIPTOR;
    }

    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return D_MODULE_FILTER;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return INDEX_VERSION;
    }

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @Override
        public Map<String, Void> map(FileContent inputData) {
            PsiFile psiFile = inputData.getPsiFile();
            if (psiFile instanceof DlangPsiFile dlangPsiFile) {
                String moduleName = dlangPsiFile.getFullyQualifiedModuleName();
                if (dlangPsiFile.getName().equals("package.d")) {
                    String directoryBasedModuleName = getDirectoryBasedModuleName(inputData);
                    // A package.d file can have a module declaration changing completely his name and so both names are valid
                    if (directoryBasedModuleName != null && !moduleName.equals(directoryBasedModuleName)) {
                        Map<String, Void> result = new HashMap<>();
                        result.put(moduleName, null);
                        result.put(directoryBasedModuleName, null);
                        return result;
                    }
                }
                return Collections.singletonMap(moduleName, null);
            }
            return Collections.emptyMap();
        }

        /**
         * Return the module name of the file based on the directory based hierarchy of the project.
         */
        private @Nullable String getDirectoryBasedModuleName(FileContent inputData) {
            String directoryBasedModuleName = null;
            VirtualFile current = inputData.getFile().getParent();
            // Consulo: FileContent#getProject() is nullable, IntelliJ's is not
            Project project = inputData.getProject();
            if (project == null) {
                return null;
            }
            VirtualFile moduleSourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(inputData.getFile());
            if (moduleSourceRoot == null) {
                return null;
            }
            while (current != null && !current.equals(moduleSourceRoot)) {
                if (directoryBasedModuleName != null) {
                    directoryBasedModuleName = current.getName() + "." + directoryBasedModuleName;
                }
                else {
                    directoryBasedModuleName = current.getName();
                }
                // upstream: current = current.parent ?: break
                current = current.getParent();
            }
            return directoryBasedModuleName;
        }
    }

    /**
     * @deprecated Only Logger and constant should be in the companion object!
     */
    @Deprecated
    public static List<DlangPsiFile> getFilesByModuleName(Project project,
                                                          String moduleName,
                                                          GlobalSearchScope searchScope) {
        PsiManager psiManager = PsiManager.getInstance(project);

        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(D_MODULE_INDEX, moduleName, searchScope);

        return ContainerUtil.mapNotNull(virtualFiles, (VirtualFile virtualFile) -> {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            return psiFile instanceof DlangPsiFile dlangPsiFile ? dlangPsiFile : null;
        });
    }
}

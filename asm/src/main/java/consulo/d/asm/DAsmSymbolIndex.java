package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.asm.language.AsmFileType;
import consulo.asm.language.impl.psi.AsmFile;
import consulo.asm.language.impl.psi.AsmLabelElement;
import consulo.asm.language.impl.psi.AsmLabelResolver;
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
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The names of the labels defined in assembly files, so the label implementing an {@code extern} D function is found
 * without parsing every assembly file of the project. Assembler local labels ({@code .L...}) and numeric labels are left
 * out: they are never linker symbols.
 */
@ExtensionImpl
public class DAsmSymbolIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("d.asm.symbol");
    private static final int VERSION = 1;

    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            PsiFile file = inputData.getPsiFile();
            for (AsmLabelElement label : PsiTreeUtil.findChildrenOfType(file, AsmLabelElement.class)) {
                if (isSymbol(label)) {
                    result.put(label.getName(), null);
                }
            }
            return result;
        };
    }

    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return (project, file) -> file.getFileType() == AsmFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @RequiredReadAction
    static boolean isSymbol(AsmLabelElement label) {
        return !label.isDirectional() && !label.getName().startsWith(".");
    }

    /**
     * The labels named {@code name} in the assembly files of {@code scope}.
     */
    @RequiredReadAction
    public static List<AsmLabelElement> findLabels(Project project, String name, GlobalSearchScope scope) {
        List<AsmLabelElement> result = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile virtualFile : FileBasedIndex.getInstance().getContainingFiles(NAME, name, scope)) {
            if (psiManager.findFile(virtualFile) instanceof AsmFile file) {
                for (AsmLabelElement label : AsmLabelResolver.getLabels(file)) {
                    if (isSymbol(label) && name.equals(label.getName())) {
                        result.add(label);
                    }
                }
            }
        }
        return result;
    }
}

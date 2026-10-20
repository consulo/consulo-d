package io.github.intellij.dlanguage.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.DumbService;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.d.language.resolve.DStubDeclarationWalker;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.resolve.DResolveUtilKt;
import io.github.intellij.dlanguage.stubs.DLanguageModuleDeclarationStub;
import io.github.intellij.dlanguage.stubs.types.ModuleDeclarationStubElementType;
import io.github.intellij.dlanguage.psi.impl.DStubElementTypes;
import io.github.intellij.dlanguage.utils.DPsiUtilKt;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// todo: consider making this class abstract so that there can be x2 classes that inherit from it.
// One for .di files (interfaces) and another for regular .d source files
public class DlangPsiFileImpl extends PsiFileBase implements DlangPsiFile {
    public DlangPsiFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, DLanguage.INSTANCE);
    }

    @Override
    public FileType getFileType() {
        return DlangFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "D Language File";
    }

    /**
     * Consulo: read from the stub when there is one, so asking another file for its module name does not parse it.
     */
    @RequiredReadAction
    private Optional<String> findModuleDeclaration() {
        StubElement<?> stub = getGreenStub();
        if (stub != null) {
            DLanguageModuleDeclarationStub declaration = stub.findChildStubByType((ModuleDeclarationStubElementType) DStubElementTypes.MODULE_DECLARATION);
            return Optional.ofNullable(declaration).map(DLanguageModuleDeclarationStub::getName);
        }
        return Optional.ofNullable(findChildByClass(DLanguageModuleDeclaration.class))
            .map(DLanguageModuleDeclaration::getIdentifierChain)
            .map(DPsiUtilKt::getImportText);
    }

    /**
     * A fully qualified module name is the package (if defined) followed by the module name.
     * Both should be lowercase ASCII characters (letters, digits, underscore). Packages correspond
     * to directory names in the source file path. Package and module names cannot be Keywords.
     * <p>
     * An example of such would be "c.stdio", where the stdio module is in the c package.
     * <p>
     * If the module name is not defined within the file then the filename (without extension) is used as the module name.
     */
    @Override
    @RequiredReadAction
    public String getFullyQualifiedModuleName() {
        Optional<String> moduleDeclaration = findModuleDeclaration();

        return moduleDeclaration.orElseGet(this::getModuleName);
    }

    /**
     * D module names are, by default, the file name with the path and extension stripped off.
     * They can be set explicitly with the module declaration.
     * This method attempts to find an explicitly defined module declaration first and then return the filename (without extension)
     * if no module declaration is found.
     */
    @Override
    @RequiredReadAction
    public String getModuleName() {
        Optional<String> moduleDeclaration = findModuleDeclaration();

        return moduleDeclaration
            .filter(StringUtil::isNotEmpty)
            .map(fullDeclaration -> {
                String[] identifiers = fullDeclaration.split("\\.");
                return identifiers[identifiers.length - 1];
            })
            .orElseGet(() -> trimExtensions(super.getName()));
    }

    @Override
    @RequiredReadAction
    public @Nullable String getPackageName() {
        return StringUtil.trimToNull(
            StringUtil.trimEnd(
                StringUtil.trimEnd(getFullyQualifiedModuleName(), this.getModuleName()),
                "."
            )
        );
    }

    @Override
    @RequiredReadAction
    public boolean processDeclarations(
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        boolean toContinue = true;
        List<String> processFiles = state.get(DResolveUtilKt.getPROCESSED_FILES_KEY());
        ResolveState newState = state;
        if (processFiles == null) {
            processFiles = new ArrayList<>();
            newState = state.put(DResolveUtilKt.getPROCESSED_FILES_KEY(), processFiles);
        }
        processFiles.add(getFullyQualifiedModuleName());
        // Consulo: a module the walk does not start in is processed from its stub tree, see DStubDeclarationWalker
        if (!this.equals(place.getContainingFile())) {
            StubElement<?> stub = getGreenStub();
            if (stub == null) {
                stub = calcStubTree().getRoot();
            }
            return DStubDeclarationWalker.processModule(this, stub, processor, state, newState, lastParent, place);
        }
        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
        for (PsiElement element : getChildren()) {
            if (element instanceof DLanguageModuleDeclaration) {
                if (!processor.execute(element, state)) {
                    toContinue = false;
                }
            }
            else if (element instanceof Declaration) {
                // Don’t reprocess the declaration we just processed.
                if (lastParent != null && lastParent.equals(element)) {
                    continue;
                }
                if (!element.processDeclarations(processor, newState, lastParent, place)) {
                    toContinue = false;
                }
                // processDeclaration may overwrite the declaration holder, so reset it before re-processing
                processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
            }
        }
        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, null);
        if (toContinue && !getFullyQualifiedModuleName().equals("object") && this.equals(place.getContainingFile())) {
            if (DumbService.isDumb(getProject())) {
                return true;
            }
            Set<DlangPsiFile> objects =
                new LinkedHashSet<>(DModuleIndex.getFilesByModuleName(getProject(), "object", GlobalSearchScope.allScope(getProject())));
            Iterator<DlangPsiFile> iterator = objects.iterator();
            DlangPsiFile objectModule = iterator.hasNext() ? (DlangPsiFile) iterator.next().getContainingFile() : null;
            toContinue = objectModule == null || objectModule.processDeclarations(processor, newState, lastParent, place);
        }
        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, null);
        return toContinue;
    }

    /**
     * @return the filename including the extension
     */
    @Override
    @RequiredReadAction
    public String getName() {
        return super.getName();
    }

    /**
     * Called when a D source file is renamed
     *
     * @param name filename of the source file being renamed
     * @return the new element after the file has been replaced
     * @throws IncorrectOperationException if the user attempts to rename the file in an illegal way. Eg:
     *                                     D file names should be composed of the ASCII characters lower case letters, digits or _
     *                                     and should also not be a Keyword.
     */
    @Override
    @RequiredWriteAction
    public PsiElement setName(String name) throws IncorrectOperationException {
        DLanguageModuleDeclaration module = findChildByClass(DLanguageModuleDeclaration.class);
        String extensionLessName = StringUtil.trimEnd(name, ".d");
        String packageName = getPackageName();
        String newModuleName = extensionLessName;
        if (!StringUtil.isEmpty(packageName)) {
            newModuleName = packageName + "." + extensionLessName;
        }

        if (module != null) {
            module.setName(newModuleName);
        }

        return super.setName(extensionLessName + ".d");
    }

    /**
     * IntelliJ's {@code StringUtil.trimExtensions}, which Consulo does not have: everything before the first dot.
     */
    private static String trimExtensions(String name) {
        int index = name.indexOf('.');
        return index < 0 ? name : name.substring(0, index);
    }
}

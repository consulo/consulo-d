package io.github.intellij.dlanguage.psi.scope.processor;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.impl.DLanguageModuleImpl;
import io.github.intellij.dlanguage.psi.impl.DLanguagePackageImpl;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.psi.scope.ElementDeclarationHint;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PackageOrModuleProcessor implements PsiScopeProcessor, ElementDeclarationHint {
    private final PsiScopeProcessor myDelegate;
    private final @Nullable DLanguagePackage myParentPackage;

    public PackageOrModuleProcessor(PsiScopeProcessor delegate, @Nullable DLanguagePackage parentPackage) {
        myDelegate = delegate;
        myParentPackage = parentPackage;
    }

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (element instanceof DLanguageImportDeclaration importDeclaration) {
            // Consulo: computed from the imported module names, which the stubs keep, instead of resolving the identifier
            // chains of the import, so the imports of a module found by a walk from another module are not parsed
            for (DLanguageSingleImport singleImport : importDeclaration.getSingleImports()) {
                if (singleImport.hasAName()) {
                    continue;
                }
                String moduleName = singleImport.getImportedModuleName();
                if (moduleName.isEmpty()) {
                    continue;
                }
                PsiElement resolvedPackage = resolvePackageOrModule(singleImport, moduleName);
                if (resolvedPackage == null) {
                    continue;
                }
                if (!myDelegate.execute(resolvedPackage, state)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The package or module named by the imported module name cut after the component that follows the parent package
     * (the first component without a parent package): what upstream resolved with the reference of the identifier chain
     * of that length.
     */
    private @Nullable PsiElement resolvePackageOrModule(PsiElement context, String moduleName) {
        List<String> parts = StringUtil.split(moduleName, ".");
        int length = parts.size();
        String parentPackage = myParentPackage != null ? myParentPackage.getQualifiedName() : null;
        while (length > 1 && !(parentPackage != null && String.join(".", parts.subList(0, length - 1)).equals(parentPackage))) {
            length--;
        }
        Project project = context.getProject();
        if (length < parts.size()) {
            return new DLanguagePackageImpl(PsiManager.getInstance(project), String.join(".", parts.subList(0, length)));
        }
        Set<DlangPsiFile> files = new LinkedHashSet<>(DModuleIndex.getFilesByModuleName(project, moduleName, GlobalSearchScope.allScope(project)));
        if (files.size() != 1) {
            return null;
        }
        DlangPsiFile file = files.iterator().next();
        return new DLanguageModuleImpl(file.getManager(), file);
    }

    @Override
    public boolean shouldProcess(ElementDeclarationHint.DeclarationKind kind) {
        return kind != ElementDeclarationHint.DeclarationKind.IMPORT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getHint(Key<T> hintKey) {
        if (ElementDeclarationHint.KEY.equals(hintKey)) {
            return (T) this;
        }
        return PsiScopeProcessor.super.getHint(hintKey);
    }
}

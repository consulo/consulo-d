package io.github.intellij.dlanguage.psi.references;

import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.PsiPolyVariantReferenceBase;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;
import io.github.intellij.dlanguage.psi.impl.DLanguageModuleImpl;
import io.github.intellij.dlanguage.psi.impl.DLanguagePackageImpl;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.github.intellij.dlanguage.utils.DPsiUtilKt.getImportText;

public class PackageReference extends PsiPolyVariantReferenceBase<DLanguageIdentifierChain> implements PsiQualifiedReference {
    public PackageReference(DLanguageIdentifierChain element, TextRange textRange) {
        super(element, textRange);
    }

    @Override
    public ResolveResult[] multiResolve(boolean incomplete) {
        if (getElement().getParent() instanceof DLanguageIdentifierChain) {
            return new ResolveResult[]{new PsiElementResolveResult(resolvePackage(getElement()))};
        }
        List<ResolveResult> results = new ArrayList<>();
        for (DlangPsiFile file : resolveModule(getElement())) {
            results.add(new PsiElementResolveResult(new DLanguageModuleImpl(file.getManager(), file)));
        }
        return results.toArray(ResolveResult.EMPTY_ARRAY);
    }

    private Set<DlangPsiFile> resolveModule(DLanguageIdentifierChain path) {
        return new HashSet<>(DModuleIndex.getFilesByModuleName(
            path.getProject(),
            getImportText(path),
            GlobalSearchScope.allScope(path.getProject())
        ));
    }

    private PsiNamedElement resolvePackage(DLanguageIdentifierChain parents) {
        Project project = parents.getProject();
        String text = getImportText(parents);
        return resolvePackageFromName(project, text);
    }

    private DLanguagePackage resolvePackageFromName(Project project, String name) {
        return new DLanguagePackageImpl(PsiManager.getInstance(project), name);
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        PsiElement identifier = myElement.getIdentifier();
        if (identifier == null) {
            throw new IncorrectOperationException();
        }
        // Renaming files is tricky: we don't want to change `RenamePsiFileProcessor`,
        // If it’s end by `.d` then it’s because we renamed a file
        String newName = StringUtil.trimEnd(newElementName, ".d");
        identifier.replace(Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(myElement.getProject(), newName)));
        return myElement;
    }

    @Override
    public @Nullable PsiElement getQualifier() {
        return myElement.getIdentifierChain();
    }

    @Override
    public String getReferenceName() {
        return Objects.requireNonNull(myElement.getIdentifier()).getText();
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return super.isReferenceTo(element);
    }
}

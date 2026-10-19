package io.github.intellij.dlanguage.structure;

import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageUnittest;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import org.jspecify.annotations.Nullable;

public class DStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    public DStructureViewModel(@Nullable Editor editor, PsiFile psiFile) {
        super(psiFile, editor, new DStructureViewElement(psiFile));
        withSuitableClasses(
            DLanguageTemplateMixinDeclaration.class,
            DLanguageTemplateDeclaration.class,
            UserDefinedType.class,
            DLanguageFunctionDeclaration.class
        );
        withSorters(Sorter.ALPHA_SORTER);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
        return structureViewTreeElement.getValue() instanceof DlangPsiFile || structureViewTreeElement.getValue() instanceof UserDefinedType;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement) {
        return false;
    }

    @Override
    protected boolean isSuitable(PsiElement element) {
        boolean suitable = super.isSuitable(element);
        if (!suitable) {
            return false;
        }

        if (PsiTreeUtil.getParentOfType(element, DLanguageUnittest.class) != null) {
            // We ignore the sub-elements in Unittests
            return false;
        }

        return true;
    }
}

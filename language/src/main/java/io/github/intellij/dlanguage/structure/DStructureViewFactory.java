package io.github.intellij.dlanguage.structure;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class DStructureViewFactory implements PsiStructureViewFactory {
    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(PsiFile psiFile) {
        return psiFile instanceof DlangPsiFile dlangPsiFile ? new DlangTreeBasedStructureViewBuilder(dlangPsiFile) : null;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

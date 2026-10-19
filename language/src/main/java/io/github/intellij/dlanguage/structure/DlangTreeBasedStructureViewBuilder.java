package io.github.intellij.dlanguage.structure;

import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import org.jspecify.annotations.Nullable;

public class DlangTreeBasedStructureViewBuilder extends TreeBasedStructureViewBuilder {
    private final DlangPsiFile myPsiFile;

    public DlangTreeBasedStructureViewBuilder(DlangPsiFile psiFile) {
        myPsiFile = psiFile;
    }

    public DlangPsiFile getPsiFile() {
        return myPsiFile;
    }

    @Override
    public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new DStructureViewModel(editor, myPsiFile);
    }
}

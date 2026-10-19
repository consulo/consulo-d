package io.github.intellij.dlanguage.highlighting.exitpoint;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerFactoryBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.psi.DlangTypes.KW_RETURN;
import static io.github.intellij.dlanguage.psi.DlangTypes.KW_THROW;

@ExtensionImpl
public class DHighlightExitPointsHandlerFactory extends HighlightUsagesHandlerFactoryBase<PsiElement> {
    @Override
    public @Nullable HighlightUsagesHandlerBase<PsiElement> createHighlightUsagesHandler(Editor editor, PsiFile file, PsiElement target) {
        if (!(file instanceof DlangPsiFile)) {
            return null;
        }

        IElementType elementType = PsiUtilCore.getElementType(target);
        if (elementType == KW_RETURN || elementType == KW_THROW) {
            return new DHighlightExitPointsHandler(editor, file, target);
        }
        return null;
    }
}

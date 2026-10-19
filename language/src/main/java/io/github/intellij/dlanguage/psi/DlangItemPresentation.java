package io.github.intellij.dlanguage.psi;

import consulo.language.psi.PsiFile;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

/**
 * @author Samael Bate (singingbush)
 * created on 20/10/18
 */
public abstract class DlangItemPresentation implements ItemPresentation {
    private final PsiFile myPsiFile;

    protected DlangItemPresentation(PsiFile psiFile) {
        myPsiFile = psiFile;
    }

    @Override
    public @Nullable String getLocationString() {
        return myPsiFile instanceof DlangPsiFile dlangPsiFile ? dlangPsiFile.getModuleName() : null;
    }

    /**
     * Upstream overrides {@code getIcon(boolean unused)}, which Consulo deprecates in favour of this method.
     */
    @Override
    public Image getIcon() {
        return DLanguage.Icons.FILE;
    }
}

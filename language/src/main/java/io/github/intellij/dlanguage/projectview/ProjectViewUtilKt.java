package io.github.intellij.dlanguage.projectview;

import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.project.ui.view.tree.PsiFileNode;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import org.jspecify.annotations.Nullable;

/**
 * Top-level declarations of upstream's {@code ProjectViewUtil.kt}.
 */
public final class ProjectViewUtilKt {
    private ProjectViewUtilKt() {
    }

    /**
     * Returns the {@link DlangPsiFile} associated with this {@link PsiFileNode}, or
     * {@code null} if at least one of the following is true for this node:
     * <ul>
     *  <li>Not part of a project.</li>
     *  <li>Has no associated {@link VirtualFile}.</li>
     *  <li>Not a D source file.</li>
     * </ul>
     */
    public static @Nullable DlangPsiFile getDlangFile(PsiFileNode node) {
        Project project = node.getProject();
        if (project == null) {
            return null;
        }
        VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        return file instanceof DlangPsiFile dlangPsiFile ? dlangPsiFile : null;
    }
}

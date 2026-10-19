package io.github.intellij.dlanguage.projectview;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.project.ui.view.tree.PsiFileNode;
import consulo.project.ui.view.tree.TreeStructureProvider;
import consulo.project.ui.view.tree.ViewSettings;
import io.github.intellij.dlanguage.psi.DlangPsiFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Modifies the Project View tree to better fit the D workflow and to take
 * advantage of features offered by the IDEA plugin API.
 * <p>
 * D source files get their own special node type here ({@link DPsiFileNode}).
 * <p>
 * Based on the Kotlin plugin's Project TreeStructureProvider:
 * <a href="https://github.com/JetBrains/kotlin/blob/26413acf33a86375e1c63b9dae9fdefbe75b0561/idea/src/org/jetbrains/kotlin/idea/projectView/projectViewProviders.kt">projectViewProviders.kt</a>
 */
@ExtensionImpl
public class DTreeStructureProvider implements TreeStructureProvider, DumbAware {
    @Override
    @SuppressWarnings("rawtypes")
    public Collection<AbstractTreeNode> modify(AbstractTreeNode parent,
                                               Collection<AbstractTreeNode> children,
                                               ViewSettings settings) {

        List<AbstractTreeNode> result = new ArrayList<>(children.size());
        for (AbstractTreeNode child : children) {
            if (child instanceof PsiFileNode psiFileNode) {
                DlangPsiFile dlangFile = ProjectViewUtilKt.getDlangFile(psiFileNode);
                if (dlangFile != null) {
                    // Consulo's BasePsiNode needs a project; getDlangFile answers a file only for a node that has one
                    result.add(new DPsiFileNode(Objects.requireNonNull(psiFileNode.getProject()), dlangFile, settings));
                }
                else {
                    result.add(child);
                }
            }
            else {
                result.add(child);
            }
        }
        return result;
    }
}

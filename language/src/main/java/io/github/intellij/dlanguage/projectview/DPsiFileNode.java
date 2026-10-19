package io.github.intellij.dlanguage.projectview;

import consulo.application.util.UserHomeFileUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.project.ui.view.tree.BasePsiNode;
import consulo.project.ui.view.tree.ViewSettings;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.tree.PresentableNodeDescriptor;
import consulo.ui.ex.tree.PresentationData;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Each instance of this class corresponds to a D language file
 * in the project view.
 */
public class DPsiFileNode extends BasePsiNode<DlangPsiFile> {
    public DPsiFileNode(Project project, DlangPsiFile dFilePsi, ViewSettings viewSettings) {
        super(project, dFilePsi, viewSettings);
    }

    @Override
    public Comparable<?> getTypeSortKey() {
        return new TypeSortKey(this);
    }

    @Override
    public @Nullable String getTitle() {
        VirtualFile virtualFile = getVirtualFile();
        return virtualFile != null ? UserHomeFileUtil.getLocationRelativeToUserHome(virtualFile.getPresentableUrl()) : super.getTitle();
    }

    @Override
    public boolean canRepresent(@Nullable Object element) {
        if (super.canRepresent(element)) {
            return true;
        }

        DlangPsiFile value = getValue();
        return value != null && element != null && element.equals(value.getVirtualFile());
    }

    @Override
    protected void updateImpl(PresentationData data) {
        DlangPsiFile filePsi = getValue();
        if (filePsi != null) {
            data.setPresentableText(filePsi.getModuleName());
            VirtualFile virtualFile = Objects.requireNonNull(filePsi.getVirtualFile());
            String fileName = virtualFile.getNameWithoutExtension();
            if (fileName.equals("package")) {
                data.addText("package." + virtualFile.getExtension(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            else {
                String presentableModuleName = filePsi.getModuleName();
                String presentableFileName =
                    !fileName.equals(presentableModuleName)
                        ? fileName + "." + virtualFile.getExtension()
                        : null;

                data.addText(presentableModuleName + "   ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
                if (presentableFileName != null) {
                    data.addText(new PresentableNodeDescriptor.ColoredFragment(LocalizeValue.of(presentableFileName), SimpleTextAttributes.GRAYED_ATTRIBUTES));
                }
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Collection<AbstractTreeNode> getChildrenImpl() {
        // TODO: Eventually the plugin could support the "Show Members" feature of the project view.
        return Collections.emptyList();
    }

    @Override
    public int getTypeSortWeight(boolean sortByType) {
        if (sortByType) {
            DlangPsiFile value = getValue();
            VirtualFile virtualFile = value == null ? null : value.getVirtualFile();
            if (virtualFile != null && virtualFile.getNameWithoutExtension().equals("package")) {
                return 1;
            }
            else {
                return 0;
            }
        }
        else {
            return super.getTypeSortWeight(sortByType);
        }
    }

    private static class TypeSortKey implements Comparable<TypeSortKey> {
        private final String myUnqualifiedModuleName;

        TypeSortKey(DPsiFileNode node) {
            myUnqualifiedModuleName = node.getValue().getModuleName();
        }

        @Override
        public int compareTo(TypeSortKey other) {
            return myUnqualifiedModuleName.compareTo(other.myUnqualifiedModuleName);
        }
    }
}

package io.github.intellij.dlanguage.features.documentation.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.intellij.dlanguage.features.documentation.DDocElementTypes.DDOC_EQ;

public class DDocKeyValueImpl extends ASTWrapperPsiElement {
    public DDocKeyValueImpl(ASTNode node) {
        super(node);
    }

    @Override
    @RequiredReadAction
    public String getName() {
        return Objects.requireNonNull(getFirstChild()).getText();
    }

    @RequiredReadAction
    public PsiElement[] getDescriptionElements() {
        // drop name and `=`, but a space may be between the two
        List<PsiElement> leafs = childLeafs(this);
        List<PsiElement> list = leafs.subList(Math.min(2, leafs.size()), leafs.size());
        if (PsiUtilCore.getElementType(list.get(0)) == DDOC_EQ) {
            list = list.subList(1, list.size());
        }
        return PsiUtilCore.toPsiElementArray(list);
    }

    /**
     * IntelliJ's {@code PsiElement.childLeafs()} ({@code psiTreeUtil.kt}), which Consulo does not have.
     */
    private static List<PsiElement> childLeafs(PsiElement element) {
        List<PsiElement> leafs = new ArrayList<>();
        PsiElement lastLeaf = PsiTreeUtil.lastChild(element);
        PsiElement leaf = PsiTreeUtil.firstChild(element);
        while (leaf != null) {
            leafs.add(leaf);
            if (leaf == lastLeaf) {
                break;
            }
            leaf = PsiTreeUtil.nextLeaf(leaf);
        }
        return leafs;
    }
}

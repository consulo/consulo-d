package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;

/**
 * Created by francis on 4/14/2017.
 */
public class DStubBasedPsiElementBase<T extends StubElement<? extends PsiElement>> extends StubBasedPsiElementBase<T> {
    public DStubBasedPsiElementBase(final T stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DStubBasedPsiElementBase(final ASTNode node) {
        super(node);
    }

    public DStubBasedPsiElementBase(final T stub, final IElementType nodeType, final ASTNode node) {
        super(stub, nodeType, node);
    }

    @Override
    public String toString() {
        return getElementType().toString();
    }
}

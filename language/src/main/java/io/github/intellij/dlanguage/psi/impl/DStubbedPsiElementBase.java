package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.stub.StubBasedPsiElementBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;

public abstract class DStubbedPsiElementBase<T extends StubElement<? extends PsiElement>> extends StubBasedPsiElementBase<T>
    implements StubBasedPsiElement<T> {
    public DStubbedPsiElementBase(final T stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DStubbedPsiElementBase(final ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return getElementType().toString();
    }

    //todo getNameIdentifier needs to use stubs
}

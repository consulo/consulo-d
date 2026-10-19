package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.NamedStubBase;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.stubs.index.DAllNameIndex;
import io.github.intellij.dlanguage.stubs.index.DMembersIndex;
import io.github.intellij.dlanguage.stubs.index.DModuleDeclarationIndex;
import io.github.intellij.dlanguage.stubs.index.DTopLevelDeclarationIndex;
import io.github.intellij.dlanguage.stubs.index.DTopLevelDeclarationsByModule;

public abstract class DNamedStubElementType<S extends NamedStubBase<T>, T extends DNamedElement> extends DStubElementType<S, T> {
    public DNamedStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public void indexStub(S stub, IndexSink sink) {
        String name = stub.getName();
        if (name == null || name.isBlank()) {
            return;
        }
        sink.occurrence(DAllNameIndex.KEY, name);
        DModuleDeclarationIndex.indexModuleDeclarations(stub, sink, name);
        DTopLevelDeclarationIndex.indexTopLevelDeclarations(stub, sink, name);
        DTopLevelDeclarationsByModule.indexTopLevelDeclarationsByModule(stub, sink);
        DMembersIndex.indexMembers(stub, sink);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return ((DNamedElement) Objects.requireNonNull(node.getPsi())).hasAName();
    }
}

package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangStructDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageStructDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class StructDeclarationStubElementType extends DNamedStubElementType<DLanguageStructDeclarationStub, DLanguageStructDeclaration> {
    public StructDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageStructDeclaration createPsi(DLanguageStructDeclarationStub stub) {
        return new DlangStructDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageStructDeclarationStub createStub(DLanguageStructDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageStructDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageStructDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageStructDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageStructDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

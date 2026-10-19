package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangInterfaceDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageInterfaceDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class InterfaceDeclarationStubElementType
    extends DNamedStubElementType<DLanguageInterfaceDeclarationStub, DLanguageInterfaceDeclaration> {
    public InterfaceDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageInterfaceDeclaration createPsi(DLanguageInterfaceDeclarationStub stub) {
        return new DlangInterfaceDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageInterfaceDeclarationStub createStub(DLanguageInterfaceDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageInterfaceDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageInterfaceDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageInterfaceDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageInterfaceDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

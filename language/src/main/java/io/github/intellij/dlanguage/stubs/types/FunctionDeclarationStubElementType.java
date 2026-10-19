package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageFunctionDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageFunctionDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class FunctionDeclarationStubElementType
    extends DNamedStubElementType<DLanguageFunctionDeclarationStub, DLanguageFunctionDeclaration> {
    public FunctionDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageFunctionDeclaration createPsi(DLanguageFunctionDeclarationStub stub) {
        return new DLanguageFunctionDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageFunctionDeclarationStub createStub(DLanguageFunctionDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageFunctionDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageFunctionDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageFunctionDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageFunctionDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

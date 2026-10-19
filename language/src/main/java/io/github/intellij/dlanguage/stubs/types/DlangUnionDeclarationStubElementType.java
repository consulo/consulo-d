package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangUnionDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DlangUnionDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DlangUnionDeclarationStubElementType extends DNamedStubElementType<DlangUnionDeclarationStub, DLanguageUnionDeclaration> {
    public DlangUnionDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageUnionDeclaration createPsi(DlangUnionDeclarationStub stub) {
        return new DlangUnionDeclarationImpl(stub, this);
    }

    @Override
    public DlangUnionDeclarationStub createStub(DLanguageUnionDeclaration psi, StubElement parentStub) {
        return new DlangUnionDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DlangUnionDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DlangUnionDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DlangUnionDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

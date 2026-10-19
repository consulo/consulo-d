package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangClassDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageClassDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class ClassDeclarationStubElementType extends DNamedStubElementType<DLanguageClassDeclarationStub, DLanguageClassDeclaration> {
    public ClassDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageClassDeclaration createPsi(DLanguageClassDeclarationStub stub) {
        return new DlangClassDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageClassDeclarationStub createStub(DLanguageClassDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageClassDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageClassDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageClassDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageClassDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

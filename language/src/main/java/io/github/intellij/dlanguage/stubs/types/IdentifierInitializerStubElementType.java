package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageIdentifierInitializerImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageIdentifierInitializerStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class IdentifierInitializerStubElementType
    extends DNamedStubElementType<DLanguageIdentifierInitializerStub, DLanguageIdentifierInitializer> {
    public IdentifierInitializerStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageIdentifierInitializer createPsi(DLanguageIdentifierInitializerStub stub) {
        return new DLanguageIdentifierInitializerImpl(stub, this);
    }

    @Override
    public DLanguageIdentifierInitializerStub createStub(DLanguageIdentifierInitializer psi, @Nullable StubElement parentStub) {
        return new DLanguageIdentifierInitializerStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageIdentifierInitializerStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageIdentifierInitializerStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageIdentifierInitializerStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

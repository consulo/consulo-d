package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageConstructorImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageConstructorStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class ConstructorStubElementType extends DNamedStubElementType<DLanguageConstructorStub, DLanguageConstructor> {
    public ConstructorStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageConstructor createPsi(DLanguageConstructorStub stub) {
        return new DLanguageConstructorImpl(stub, this);
    }

    @Override
    public DLanguageConstructorStub createStub(DLanguageConstructor psi, @Nullable StubElement parentStub) {
        return new DLanguageConstructorStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageConstructorStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageConstructorStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageConstructorStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

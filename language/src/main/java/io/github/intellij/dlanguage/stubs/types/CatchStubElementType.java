package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageCatchImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageCatch;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageCatchStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class CatchStubElementType extends DNamedStubElementType<DLanguageCatchStub, DLanguageCatch> {
    public CatchStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageCatch createPsi(DLanguageCatchStub stub) {
        return new DLanguageCatchImpl(stub, this);
    }

    @Override
    public DLanguageCatchStub createStub(DLanguageCatch psi, StubElement parentStub) {
        return new DLanguageCatchStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageCatchStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageCatchStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageCatchStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageForeachTypeImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageForeachType;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageForeachTypeStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangForeachTypeStubElementType extends DNamedStubElementType<DLanguageForeachTypeStub, DLanguageForeachType> {
    public DlangForeachTypeStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageForeachType createPsi(DLanguageForeachTypeStub stub) {
        return new DLanguageForeachTypeImpl(stub, this);
    }

    @Override
    public DLanguageForeachTypeStub createStub(DLanguageForeachType psi, StubElement parentStub) {
        return new DLanguageForeachTypeStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageForeachTypeStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageForeachTypeStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageForeachTypeStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

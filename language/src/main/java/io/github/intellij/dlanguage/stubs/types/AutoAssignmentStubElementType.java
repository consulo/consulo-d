package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageAutoAssignmentImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageAutoAssignmentStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class AutoAssignmentStubElementType extends DNamedStubElementType<DLanguageAutoAssignmentStub, DLanguageAutoAssignment> {
    public AutoAssignmentStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageAutoAssignment createPsi(DLanguageAutoAssignmentStub stub) {
        return new DLanguageAutoAssignmentImpl(stub, this);
    }

    @Override
    public DLanguageAutoAssignmentStub createStub(DLanguageAutoAssignment psi, @Nullable StubElement parentStub) {
        return new DLanguageAutoAssignmentStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageAutoAssignmentStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageAutoAssignmentStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageAutoAssignmentStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

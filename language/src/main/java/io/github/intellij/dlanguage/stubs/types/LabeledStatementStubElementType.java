package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageLabeledStatement;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageLabeledStatementImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageLabeledStatementStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class LabeledStatementStubElementType extends DNamedStubElementType<DLanguageLabeledStatementStub, DLanguageLabeledStatement> {
    public LabeledStatementStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageLabeledStatement createPsi(DLanguageLabeledStatementStub stub) {
        return new DLanguageLabeledStatementImpl(stub, this);
    }

    @Override
    public DLanguageLabeledStatementStub createStub(DLanguageLabeledStatement psi, @Nullable StubElement parentStub) {
        return new DLanguageLabeledStatementStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageLabeledStatementStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageLabeledStatementStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageLabeledStatementStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

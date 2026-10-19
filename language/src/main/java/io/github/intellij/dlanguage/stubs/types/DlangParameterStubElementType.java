package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageParameterImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageParameterStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangParameterStubElementType extends DNamedStubElementType<DLanguageParameterStub, DLanguageParameter> {
    public DlangParameterStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageParameter createPsi(DLanguageParameterStub stub) {
        return new DLanguageParameterImpl(stub, this);
    }

    @Override
    public DLanguageParameterStub createStub(DLanguageParameter psi, StubElement parentStub) {
        return new DLanguageParameterStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageParameterStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageParameterStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageParameterStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

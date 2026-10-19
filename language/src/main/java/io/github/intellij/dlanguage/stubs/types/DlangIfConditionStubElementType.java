package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageIfCondition;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageIfConditionImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageIfConditionStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangIfConditionStubElementType extends DNamedStubElementType<DLanguageIfConditionStub, DLanguageIfCondition> {
    public DlangIfConditionStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageIfCondition createPsi(DLanguageIfConditionStub stub) {
        return new DLanguageIfConditionImpl(stub, this);
    }

    @Override
    public DLanguageIfConditionStub createStub(DLanguageIfCondition psi, StubElement parentStub) {
        return new DLanguageIfConditionStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageIfConditionStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageIfConditionStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageIfConditionStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

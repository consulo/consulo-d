package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageAliasInitializerImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageAliasInitializerStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class AliasInitializerStubElementType extends DNamedStubElementType<DLanguageAliasInitializerStub, DLanguageAliasInitializer> {
    public AliasInitializerStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageAliasInitializer createPsi(DLanguageAliasInitializerStub stub) {
        return new DLanguageAliasInitializerImpl(stub, this);
    }

    @Override
    public DLanguageAliasInitializerStub createStub(DLanguageAliasInitializer psi, StubElement parentStub) {
        return new DLanguageAliasInitializerStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageAliasInitializerStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageAliasInitializerStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageAliasInitializerStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageNamedImportBindImpl;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageNamedImportBindStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DLanguageNamedImportBindStubElementType extends DNamedStubElementType<DLanguageNamedImportBindStub, DLanguageNamedImportBind> {
    public DLanguageNamedImportBindStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageNamedImportBind createPsi(DLanguageNamedImportBindStub stub) {
        return new DLanguageNamedImportBindImpl(stub, this);
    }

    @Override
    public DLanguageNamedImportBindStub createStub(DLanguageNamedImportBind psi, @Nullable StubElement parentStub) {
        return new DLanguageNamedImportBindStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageNamedImportBindStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageNamedImportBindStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageNamedImportBindStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

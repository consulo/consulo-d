package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangDeclaratorIdentifierImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageDeclaratorIdentifier;
import io.github.intellij.dlanguage.stubs.DLanguageDeclaratorIdentifierStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DeclaratorIdentifierStubElementType
    extends DNamedStubElementType<DLanguageDeclaratorIdentifierStub, DLanguageDeclaratorIdentifier> {
    public DeclaratorIdentifierStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageDeclaratorIdentifier createPsi(DLanguageDeclaratorIdentifierStub stub) {
        return new DlangDeclaratorIdentifierImpl(stub, this);
    }

    @Override
    public DLanguageDeclaratorIdentifierStub createStub(DLanguageDeclaratorIdentifier psi, @Nullable StubElement parentStub) {
        return new DLanguageDeclaratorIdentifierStub(parentStub, this, psi.getName(), null);
    }

    @Override
    public void serialize(DLanguageDeclaratorIdentifierStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
    }

    @Override
    public DLanguageDeclaratorIdentifierStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageDeclaratorIdentifierStub(parentStub, this, dataStream.readName(), null);
    }
}

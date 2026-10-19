package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DLanguageEnumMemberImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageEnumMemberStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 6/13/2017.
 */
public class DlangEnumMemberStubElementType extends DNamedStubElementType<DLanguageEnumMemberStub, DLanguageEnumMember> {
    public DlangEnumMemberStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageEnumMember createPsi(DLanguageEnumMemberStub stub) {
        return new DLanguageEnumMemberImpl(stub, this);
    }

    @Override
    public DLanguageEnumMemberStub createStub(DLanguageEnumMember psi, StubElement parentStub) {
        return new DLanguageEnumMemberStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageEnumMemberStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageEnumMemberStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageEnumMemberStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

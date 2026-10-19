package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.index.io.StringRef;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangVersionSpecificationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageVersionSpecificationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 1/5/2018.
 */
public class VersionSpecificationElementType
    extends DNamedStubElementType<DLanguageVersionSpecificationStub, DLanguageVersionSpecification> {
    public VersionSpecificationElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageVersionSpecification createPsi(DLanguageVersionSpecificationStub stub) {
        return new DlangVersionSpecificationImpl(stub, this);
    }

    @Override
    public DLanguageVersionSpecificationStub createStub(DLanguageVersionSpecification psi, @Nullable StubElement parentStub) {
        return new DLanguageVersionSpecificationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public DLanguageVersionSpecificationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        StringRef name = dataStream.readName();
        DAttributes attributes = DAttributes.read(dataStream);
        return new DLanguageVersionSpecificationStub(parentStub, this, name != null ? name.getString() : null, attributes);
    }

    @Override
    public void serialize(DLanguageVersionSpecificationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }
}

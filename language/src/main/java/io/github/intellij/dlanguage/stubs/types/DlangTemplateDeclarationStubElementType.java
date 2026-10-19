package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangTemplateDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DlangTemplateDeclarationStubElementType
    extends DNamedStubElementType<DLanguageTemplateDeclarationStub, DLanguageTemplateDeclaration> {
    public DlangTemplateDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageTemplateDeclaration createPsi(DLanguageTemplateDeclarationStub stub) {
        return new DlangTemplateDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageTemplateDeclarationStub createStub(DLanguageTemplateDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageTemplateDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageTemplateDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageTemplateDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageTemplateDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

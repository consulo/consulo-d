package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangEnumDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageEnumDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 3/9/2017.
 */
public class EnumDeclarationStubElementType extends DNamedStubElementType<DLanguageEnumDeclarationStub, DLanguageEnumDeclaration> {
    public EnumDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageEnumDeclaration createPsi(DLanguageEnumDeclarationStub stub) {
        return new DlangEnumDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageEnumDeclarationStub createStub(DLanguageEnumDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageEnumDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageEnumDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageEnumDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageEnumDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

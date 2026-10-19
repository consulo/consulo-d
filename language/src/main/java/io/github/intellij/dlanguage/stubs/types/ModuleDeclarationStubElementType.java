package io.github.intellij.dlanguage.stubs.types;

import java.util.Objects;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.impl.named.DlangModuleDeclarationImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.DLanguageModuleDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class ModuleDeclarationStubElementType extends DNamedStubElementType<DLanguageModuleDeclarationStub, DLanguageModuleDeclaration> {
    public ModuleDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageModuleDeclaration createPsi(DLanguageModuleDeclarationStub stub) {
        return new DlangModuleDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageModuleDeclarationStub createStub(DLanguageModuleDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageModuleDeclarationStub(parentStub, this, psi.getName(), psi.getAttributes());
    }

    @Override
    public void serialize(DLanguageModuleDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        Objects.requireNonNull(stub.getAttributes()).write(dataStream);
    }

    @Override
    public DLanguageModuleDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageModuleDeclarationStub(parentStub, this, dataStream.readName(), DAttributes.read(dataStream));
    }
}

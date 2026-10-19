package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.impl.DLanguageImportDeclarationImpl;
import io.github.intellij.dlanguage.psi.interfaces.DVisibility;
import io.github.intellij.dlanguage.psi.interfaces.DVisibilityKt;
import io.github.intellij.dlanguage.psi.interfaces.DVisibilityStubKind;
import io.github.intellij.dlanguage.stubs.DLanguageImportDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DLanguageImportDeclarationStubElementType
    extends DStubElementType<DLanguageImportDeclarationStub, DLanguageImportDeclaration> {
    public DLanguageImportDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageImportDeclaration createPsi(DLanguageImportDeclarationStub stub) {
        return new DLanguageImportDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageImportDeclarationStub createStub(DLanguageImportDeclaration psi, StubElement parentStub) {
        return new DLanguageImportDeclarationStub(
            parentStub,
            this,
            DVisibilityKt.getStubKind(psi.visibility()),
            psi.visibility() instanceof DVisibility.Package packageVisibility ? packageVisibility.getName() : null
        );
    }

    @Override
    public void serialize(DLanguageImportDeclarationStub stub, StubOutputStream dataStream) throws IOException {
        dataStream.writeByte(stub.getVisibility().ordinal());
        if (stub.getVisibility() == DVisibilityStubKind.PACKAGE_SPECIFIC) {
            dataStream.writeUTF(stub.getVisibility().name());
        }
    }

    @Override
    public DLanguageImportDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        DVisibilityStubKind visibilityKind = DVisibilityStubKind.values()[dataStream.readUnsignedByte()];

        @Nullable String packageNameRestriction = visibilityKind == DVisibilityStubKind.PACKAGE_SPECIFIC
            ? dataStream.readUTF()
            : null;
        return new DLanguageImportDeclarationStub(
            parentStub,
            this,
            visibilityKind,
            packageNameRestriction
        );
    }
}

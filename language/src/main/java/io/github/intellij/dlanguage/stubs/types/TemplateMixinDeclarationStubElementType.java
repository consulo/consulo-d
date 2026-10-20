package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.impl.DLanguageTemplateMixinDeclarationImpl;
import io.github.intellij.dlanguage.stubs.DLanguageTemplateMixinDeclarationStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class TemplateMixinDeclarationStubElementType
    extends DStubElementType<DLanguageTemplateMixinDeclarationStub, DLanguageTemplateMixinDeclaration> {
    public TemplateMixinDeclarationStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageTemplateMixinDeclaration createPsi(DLanguageTemplateMixinDeclarationStub stub) {
        return new DLanguageTemplateMixinDeclarationImpl(stub, this);
    }

    @Override
    public DLanguageTemplateMixinDeclarationStub createStub(DLanguageTemplateMixinDeclaration psi, @Nullable StubElement parentStub) {
        return new DLanguageTemplateMixinDeclarationStub(parentStub, this);
    }

    @Override
    public DLanguageTemplateMixinDeclarationStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageTemplateMixinDeclarationStub(parentStub, this);
    }
}

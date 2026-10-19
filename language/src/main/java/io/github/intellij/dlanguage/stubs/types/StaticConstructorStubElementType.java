package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.DLanguageStaticConstructor;
import io.github.intellij.dlanguage.psi.impl.DLanguageStaticConstructorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageStaticConstructorStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class StaticConstructorStubElementType extends DStubElementType<DLanguageStaticConstructorStub, DLanguageStaticConstructor> {
    public StaticConstructorStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageStaticConstructor createPsi(DLanguageStaticConstructorStub stub) {
        return new DLanguageStaticConstructorImpl(stub, this);
    }

    @Override
    public DLanguageStaticConstructorStub createStub(DLanguageStaticConstructor psi, @Nullable StubElement parentStub) {
        return new DLanguageStaticConstructorStub(parentStub, this);
    }

    @Override
    public DLanguageStaticConstructorStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageStaticConstructorStub(parentStub, this);
    }
}

package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.DLanguageSharedStaticConstructor;
import io.github.intellij.dlanguage.psi.impl.DLanguageSharedStaticConstructorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageSharedStaticConstructorStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class SharedStaticConstructorStubElementType
    extends DStubElementType<DLanguageSharedStaticConstructorStub, DLanguageSharedStaticConstructor> {
    public SharedStaticConstructorStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageSharedStaticConstructor createPsi(DLanguageSharedStaticConstructorStub stub) {
        return new DLanguageSharedStaticConstructorImpl(stub, this);
    }

    @Override
    public DLanguageSharedStaticConstructorStub createStub(DLanguageSharedStaticConstructor psi, @Nullable StubElement parentStub) {
        return new DLanguageSharedStaticConstructorStub(parentStub, this);
    }

    @Override
    public DLanguageSharedStaticConstructorStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageSharedStaticConstructorStub(parentStub, this);
    }
}

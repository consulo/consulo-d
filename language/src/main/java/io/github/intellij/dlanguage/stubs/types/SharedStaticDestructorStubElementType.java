package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.DLanguageSharedStaticDestructor;
import io.github.intellij.dlanguage.psi.impl.DLanguageSharedStaticDestructorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageSharedStaticDestructorStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class SharedStaticDestructorStubElementType
    extends DStubElementType<DLanguageSharedStaticDestructorStub, DLanguageSharedStaticDestructor> {
    public SharedStaticDestructorStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageSharedStaticDestructor createPsi(DLanguageSharedStaticDestructorStub stub) {
        return new DLanguageSharedStaticDestructorImpl(stub, this);
    }

    @Override
    public DLanguageSharedStaticDestructorStub createStub(DLanguageSharedStaticDestructor psi, @Nullable StubElement parentStub) {
        return new DLanguageSharedStaticDestructorStub(parentStub, this);
    }

    @Override
    public DLanguageSharedStaticDestructorStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub)
        throws IOException {
        return new DLanguageSharedStaticDestructorStub(parentStub, this);
    }
}

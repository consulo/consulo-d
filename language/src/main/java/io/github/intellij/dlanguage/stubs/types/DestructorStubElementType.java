package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.impl.DLanguageDestructorImpl;
import io.github.intellij.dlanguage.psi.named.DLanguageDestructor;
import io.github.intellij.dlanguage.stubs.DLanguageDestructorStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class DestructorStubElementType extends DStubElementType<DLanguageDestructorStub, DLanguageDestructor> {
    public DestructorStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageDestructor createPsi(DLanguageDestructorStub stub) {
        return new DLanguageDestructorImpl(stub, this);
    }

    @Override
    public DLanguageDestructorStub createStub(DLanguageDestructor psi, @Nullable StubElement parentStub) {
        return new DLanguageDestructorStub(parentStub, this);
    }

    @Override
    public DLanguageDestructorStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageDestructorStub(parentStub, this);
    }
}

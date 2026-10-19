package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.DLanguageStaticDestructor;
import io.github.intellij.dlanguage.psi.impl.DLanguageStaticDestructorImpl;
import io.github.intellij.dlanguage.stubs.DLanguageStaticDestructorStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class StaticDestructorStubElementType extends DStubElementType<DLanguageStaticDestructorStub, DLanguageStaticDestructor> {
    public StaticDestructorStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageStaticDestructor createPsi(DLanguageStaticDestructorStub stub) {
        return new DLanguageStaticDestructorImpl(stub, this);
    }

    @Override
    public DLanguageStaticDestructorStub createStub(DLanguageStaticDestructor psi, @Nullable StubElement parentStub) {
        return new DLanguageStaticDestructorStub(parentStub, this);
    }

    @Override
    public DLanguageStaticDestructorStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageStaticDestructorStub(parentStub, this);
    }
}

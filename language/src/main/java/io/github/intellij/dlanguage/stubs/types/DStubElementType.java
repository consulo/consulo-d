package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubOutputStream;
import io.github.intellij.dlanguage.DLanguage;

import java.io.IOException;

public abstract class DStubElementType<S extends StubElement<T>, T extends PsiElement> extends IStubElementType<S, T> {
    public DStubElementType(String debugName) {
        super(debugName, DLanguage.INSTANCE);
    }

    @Override
    public void indexStub(S stub, IndexSink sink) {
    }

    @Override
    public void serialize(S stub, StubOutputStream dataStream) throws IOException {
    }

    @Override
    public String getExternalId() {
        return "d." + super.toString();
    }
}

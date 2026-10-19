package io.github.intellij.dlanguage.stubs.types;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import io.github.intellij.dlanguage.psi.DLanguageUnittest;
import io.github.intellij.dlanguage.psi.impl.DlangUnittestImpl;
import io.github.intellij.dlanguage.stubs.DLanguageUnittestStub;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Created by francis on 4/8/2017.
 */
public class UnittestStubElementType extends DStubElementType<DLanguageUnittestStub, DLanguageUnittest> {
    public UnittestStubElementType(String debugName) {
        super(debugName);
    }

    @Override
    public DLanguageUnittest createPsi(DLanguageUnittestStub stub) {
        return new DlangUnittestImpl(stub, this);
    }

    @Override
    public DLanguageUnittestStub createStub(DLanguageUnittest psi, @Nullable StubElement parentStub) {
        return new DLanguageUnittestStub(parentStub, this);
    }

    @Override
    public DLanguageUnittestStub deserialize(StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
        return new DLanguageUnittestStub(parentStub, this);
    }
}

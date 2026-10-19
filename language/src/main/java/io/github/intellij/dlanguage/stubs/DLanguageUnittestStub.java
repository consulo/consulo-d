package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageUnittest;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 4/7/2017.
 */
public class DLanguageUnittestStub extends StubBase<DLanguageUnittest> {

    public DLanguageUnittestStub(final @Nullable StubElement parent, final IStubElementType elementType) {
        super(parent, elementType);
    }

}

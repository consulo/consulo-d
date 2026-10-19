package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageSharedStaticConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 1/14/2017.
 */
public class DLanguageSharedStaticConstructorStub extends StubBase<DLanguageSharedStaticConstructor> implements StubElement<DLanguageSharedStaticConstructor> {

    public DLanguageSharedStaticConstructorStub(final @Nullable StubElement parent, final IStubElementType elementType) {
        super(parent, elementType);
    }
}

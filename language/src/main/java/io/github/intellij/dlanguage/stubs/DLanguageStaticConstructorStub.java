package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageStaticConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 1/14/2017.
 */
public class DLanguageStaticConstructorStub extends StubBase<DLanguageStaticConstructor> implements StubElement<DLanguageStaticConstructor> {
    public DLanguageStaticConstructorStub(final @Nullable StubElement parent, final IStubElementType elementType) {
        super(parent, elementType);
    }


}

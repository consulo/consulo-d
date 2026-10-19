package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

/**
 * Created by franc on 1/14/2017.
 */
public class DLanguageConstructorStub extends DNamedStubBase<DLanguageConstructor> {

    public DLanguageConstructorStub(final @Nullable StubElement parent,
                                    final IStubElementType elementType, final @Nullable StringRef name,
                                    @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DLanguageConstructorStub(final @Nullable StubElement parent,
                                    final IStubElementType elementType, final @Nullable String name,
                                    @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

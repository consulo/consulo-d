package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis nixon on 1/11/2017.
 */
public class DLanguageIdentifierInitializerStub extends DNamedStubBase<DLanguageIdentifierInitializer> {

    public DLanguageIdentifierInitializerStub(final @Nullable StubElement parent,
                                              final IStubElementType elementType, final @Nullable StringRef name,
                                              @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DLanguageIdentifierInitializerStub(final @Nullable StubElement parent, final IStubElementType elementType,
                                              final @Nullable String name,
                                              @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

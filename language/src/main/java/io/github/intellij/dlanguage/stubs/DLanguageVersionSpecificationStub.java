package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 1/5/2018.
 */
public class DLanguageVersionSpecificationStub extends DNamedStubBase<DLanguageVersionSpecification> {

    protected DLanguageVersionSpecificationStub(final @Nullable StubElement parent,
                                                final IStubElementType elementType,
                                                final @Nullable StringRef name,
                                                final @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DLanguageVersionSpecificationStub(final @Nullable StubElement parent,
                                             final IStubElementType elementType, final @Nullable String name,
                                             final @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

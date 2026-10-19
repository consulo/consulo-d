package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.NamedStubBase;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 12/3/2017.
 */
public class DNamedStubBase<T extends DNamedElement> extends NamedStubBase<T> {

    private final @Nullable DAttributes attributes;

    protected DNamedStubBase(final @Nullable StubElement parent,
                             final IStubElementType elementType,
                             final @Nullable StringRef name,
                             final @Nullable DAttributes attributes) {
        super(parent, elementType, name);
        this.attributes = attributes;
    }

    protected DNamedStubBase(final @Nullable StubElement parent,
                             final IStubElementType elementType,
                             final @Nullable String name,
                             final @Nullable DAttributes attributes) {
        super(parent, elementType, name);
        this.attributes = attributes;
    }

    public @Nullable DAttributes getAttributes() {
        return attributes;
    }
}

package io.github.intellij.dlanguage.stubs;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.d.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;

/**
 * Created by francis on 12/3/2017.
 */
public class DNamedStubBase<T extends DNamedElement> extends NamedStubBase<T> {

    private final DAttributes attributes;

    protected DNamedStubBase(final StubElement parent,
        @NotNull final IStubElementType elementType,
        final StringRef name,
        final DAttributes attributes) {
        super(parent, elementType, name);
        this.attributes = attributes;
    }

    protected DNamedStubBase(final StubElement parent,
        @NotNull final IStubElementType elementType, final String name,
        final DAttributes attributes) {
        super(parent, elementType, name);
        this.attributes = attributes;
    }

    public DAttributes getAttributes() {
        return attributes;
    }
}

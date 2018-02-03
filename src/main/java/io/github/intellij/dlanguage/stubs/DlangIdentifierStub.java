package io.github.intellij.dlanguage.stubs;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.d.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.psi.named.DlangIdentifier;

public class DlangIdentifierStub extends DNamedStubBase<DlangIdentifier> {

    public DlangIdentifierStub(final StubElement parent, final IStubElementType elementType,
        final StringRef name,
        DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DlangIdentifierStub(final StubElement parent, final IStubElementType elementType,
        final String name,
        DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

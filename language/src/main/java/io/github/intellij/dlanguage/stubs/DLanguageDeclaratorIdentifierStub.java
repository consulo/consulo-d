package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.named.DLanguageDeclaratorIdentifier;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

public class DLanguageDeclaratorIdentifierStub extends DNamedStubBase<DLanguageDeclaratorIdentifier> {
    public DLanguageDeclaratorIdentifierStub(@Nullable StubElement parent, IStubElementType elementType, @Nullable StringRef name, @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
    public DLanguageDeclaratorIdentifierStub(@Nullable StubElement parent, IStubElementType elementType, @Nullable String name, @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

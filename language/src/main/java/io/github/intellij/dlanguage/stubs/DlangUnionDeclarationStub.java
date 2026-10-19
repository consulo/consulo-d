package io.github.intellij.dlanguage.stubs;

import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

public class DlangUnionDeclarationStub extends DNamedStubBase<DLanguageUnionDeclaration> {
    public DlangUnionDeclarationStub(@Nullable StubElement<?> parent, IStubElementType<?, ?> elementType, @Nullable StringRef name, @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DlangUnionDeclarationStub(@Nullable StubElement<?> parent, IStubElementType<?, ?> elementType, @Nullable String name, @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

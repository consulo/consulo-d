package io.github.intellij.dlanguage.stubs;

import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 6/13/2017.
 */
public class DLanguageEnumMemberStub extends DNamedStubBase<DLanguageEnumMember> {
    public DLanguageEnumMemberStub(@Nullable StubElement<?> parent, IStubElementType<?, ?> elementType, @Nullable StringRef name, @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DLanguageEnumMemberStub(@Nullable StubElement<?> parent, IStubElementType<?, ?> elementType, @Nullable String name, @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

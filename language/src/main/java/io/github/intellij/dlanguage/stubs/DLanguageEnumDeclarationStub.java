package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;
import consulo.index.io.StringRef;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.stubs.interfaces.HasMembersStub;
import org.jspecify.annotations.Nullable;

/**
 * Created by francis on 3/9/2017.
 */
public class DLanguageEnumDeclarationStub extends DNamedStubBase<DLanguageEnumDeclaration> implements
    HasMembersStub {

    public DLanguageEnumDeclarationStub(final @Nullable StubElement parent, final IStubElementType elementType,
                                        final @Nullable StringRef name,
                                        final @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DLanguageEnumDeclarationStub(final @Nullable StubElement parent, final IStubElementType elementType,
                                        final @Nullable String name,
                                        final @Nullable DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

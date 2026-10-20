package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * Consulo: a {@code mixin template} is stubbed so that its declarations are not taken for module level ones when a module
 * is processed from its stubs.
 */
public class DLanguageTemplateMixinDeclarationStub extends StubBase<DLanguageTemplateMixinDeclaration> {
    public DLanguageTemplateMixinDeclarationStub(@Nullable StubElement<?> parent, IStubElementType<?, ?> elementType) {
        super(parent, elementType);
    }
}

package io.github.intellij.dlanguage.stubs;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.d.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.psi.named.DlangTemplateDeclaration;

/**
 * Created by francis nixon on 1/11/2017.
 */
public class DlangTemplateDeclarationStub extends DNamedStubBase<DlangTemplateDeclaration> {

    public DlangTemplateDeclarationStub(final StubElement parent,
        @NotNull final IStubElementType elementType, final StringRef name,
        final DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DlangTemplateDeclarationStub(final StubElement parent,
        final IStubElementType elementType, final String name,
        final DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

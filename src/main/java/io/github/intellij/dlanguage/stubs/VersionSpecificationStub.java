package io.github.intellij.dlanguage.stubs;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.d.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.psi.named.DLanguageVersionSpecification;

/**
 * Created by francis on 1/5/2018.
 */
public class VersionSpecificationStub extends DNamedStubBase<DLanguageVersionSpecification> {

    protected VersionSpecificationStub(final StubElement parent,
        @NotNull final IStubElementType elementType,
        final StringRef name,
        final DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public VersionSpecificationStub(final StubElement parent,
        @NotNull final IStubElementType elementType, final String name,
        final DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

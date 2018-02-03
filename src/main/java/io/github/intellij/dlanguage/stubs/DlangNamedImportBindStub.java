package io.github.intellij.dlanguage.stubs;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.d.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;

/**
 * Created by francis on 8/8/2017.
 */
public class DlangNamedImportBindStub extends DNamedStubBase<DLanguageNamedImportBind> {

    public DlangNamedImportBindStub(final StubElement parent,
        @NotNull final IStubElementType elementType, final StringRef name,
        DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }

    public DlangNamedImportBindStub(final StubElement parent,
        @NotNull final IStubElementType elementType, final String name,
        DAttributes attributes) {
        super(parent, elementType, name, attributes);
    }
}

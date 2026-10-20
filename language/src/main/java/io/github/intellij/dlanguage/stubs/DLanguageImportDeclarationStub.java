package io.github.intellij.dlanguage.stubs;

import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.interfaces.DVisibilityStubKind;
import org.jspecify.annotations.Nullable;

public class DLanguageImportDeclarationStub extends StubBase<DLanguageImportDeclaration> {
    private final @Nullable StubElement<?> myParentElement;
    private final IStubElementType<?, ?> myElementType;
    private final DVisibilityStubKind myVisibility;
    private final @Nullable String myPackageVisibilityName;
    private final boolean myStatic;

    public DLanguageImportDeclarationStub(@Nullable StubElement<?> parent,
                                          IStubElementType<?, ?> elementType,
                                          DVisibilityStubKind visibility,
                                          @Nullable String packageVisibilityName,
                                          boolean isStatic) {
        super(parent, elementType);
        myParentElement = parent;
        myElementType = elementType;
        myVisibility = visibility;
        myPackageVisibilityName = packageVisibilityName;
        myStatic = isStatic;
    }

    public @Nullable StubElement<?> getParent() {
        return myParentElement;
    }

    public IStubElementType<?, ?> getElementType() {
        return myElementType;
    }

    public DVisibilityStubKind getVisibility() {
        return myVisibility;
    }

    public @Nullable String getPackageVisibilityName() {
        return myPackageVisibilityName;
    }

    /**
     * Consulo: {@code static import}, so a walk from another module does not need the AST to know it.
     */
    public boolean isStatic() {
        return myStatic;
    }
}

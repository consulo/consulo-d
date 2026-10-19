package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import io.github.intellij.dlanguage.psi.DlangItemPresentation;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder.Visibility;
import io.github.intellij.dlanguage.stubs.DNamedStubBase;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract class DNamedStubbedPsiElementBase<T extends DNamedStubBase<? extends PsiElement>> extends
    DStubbedPsiElementBase<T> implements DNamedElement {

    public DNamedStubbedPsiElementBase(final T stub, final IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public DNamedStubbedPsiElementBase(final ASTNode node) {
        super(node);
    }

    public abstract @Nullable PsiElement getNameIdentifier();

    public @Nullable String getName() {
        final T stub = getGreenStub();
        final String name = stub != null ? stub.getName() : null;
        if (name != null) {
            return name;
        }

        final PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getText()
            : null;
    }

    @Override
    public int getTextOffset() {
        return getNameIdentifier() != null ? getNameIdentifier().getTextOffset(): super.getTextOffset();
    }

    public PsiElement setName(final String newName) {
        if (getNameIdentifier() == null) {
            throw new IncorrectOperationException("Cannot rename. Identifier was Null");
        }
        getNameIdentifier().replace(
            Objects.requireNonNull(DElementFactory.createDLanguageIdentifierFromText(
                getProject(),
                newName
            ))
        );
        return this;
    }

    public ItemPresentation getPresentation() {
        return new DlangItemPresentation(getContainingFile()) {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.ofNullable(getName());
            }
        };
    }

    public @Nullable DAttributes getAttributes() {
        //todo this can be slow and have an O(n^2) complexity during indexing
        if (getGreenStub() != null) {
            return getGreenStub().getAttributes();
        }
        final DAttributesFinder finder = new DAttributesFinder(this);
        finder.recurseUp();
        return finder.getAttributes();
    }

    @Override
    public Visibility visibility() {
        return Objects.requireNonNull(getAttributes()).getVisibility();
    }

    public boolean isPublic() {
        return Objects.requireNonNull(getAttributes()).getVisibility() == Visibility.PUBLIC;
    }

    public boolean isProtected() {
        return Objects.requireNonNull(getAttributes()).getVisibility() == Visibility.PROTECTED;
    }

    public boolean isPrivate() {
        return Objects.requireNonNull(getAttributes()).getVisibility() == Visibility.PRIVATE;
    }

    public boolean isProperty() {
        return Objects.requireNonNull(getAttributes()).getProperty();
    }

    public boolean isNoGC() {
        return Objects.requireNonNull(getAttributes()).getNoGC();
    }

    public boolean isExtern() {
        return Objects.requireNonNull(getAttributes()).getExtern();
    }

    public boolean isPure() {
        return Objects.requireNonNull(getAttributes()).getPure();
    }

    public boolean isNothrow() {
        return Objects.requireNonNull(getAttributes()).getNothrow();
    }

    public boolean isConst() {
        return Objects.requireNonNull(getAttributes()).getConst();
    }

    public boolean isImmutable() {
        return Objects.requireNonNull(getAttributes()).getImmutable();
    }

    public boolean isEnum() {
        return Objects.requireNonNull(getAttributes()).getEnum();
    }

}

package io.github.intellij.dlanguage.psi.interfaces;

import consulo.navigation.NavigationItem;
import consulo.language.psi.PsiNameIdentifierOwner;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;

/**
 * Interface that combines everything we need for convenient navigation.
 */
// The PsiNameIdentifierOwner is necessary for the in-place rename refactoring.
// PsiNamedElement seems like it should be enough, but it's not.
public interface DNamedElement extends PsiNameIdentifierOwner, NavigationItem {

//    default String getFullName() {
//        return DPsiImplUtil.getFullName(this);
//    }

    @Override
    @Nullable String getName();

    @Nullable DAttributes getAttributes();

    default boolean hasAName() {
        return getName() != null && !getName().isBlank();
    }

    boolean isPublic();

    boolean isProtected();

    boolean isPrivate();

    DAttributesFinder.Visibility visibility();

    boolean isProperty();

    boolean isNoGC();

    boolean isExtern();

    boolean isPure();

    boolean isNothrow();

    boolean isConst();

    boolean isImmutable();

    boolean isEnum();
}

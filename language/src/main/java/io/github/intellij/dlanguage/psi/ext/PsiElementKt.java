package io.github.intellij.dlanguage.psi.ext;

import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Port of upstream {@code PsiElement.kt}; the Kotlin {@code Sequence} extension properties are lazy {@link Stream}s.
 */
public final class PsiElementKt {
    private PsiElementKt() {
    }

    public static Stream<PsiElement> getAncestors(PsiElement element) {
        return generate(element, PsiElement::getParent);
    }

    public static Stream<PsiElement> getLeftLeaves(PsiElement element) {
        return generate(element, PsiTreeUtil::prevLeaf).skip(1);
    }

    public static Stream<PsiElement> getRightSiblings(PsiElement element) {
        return generate(element.getNextSibling(), PsiElement::getNextSibling);
    }

    public static Stream<PsiElement> getLeftSiblings(PsiElement element) {
        return generate(element.getPrevSibling(), PsiElement::getPrevSibling);
    }

    /**
     * Finds first sibling that is neither comment, nor whitespace before given element.
     */
    public static @Nullable PsiElement getPrevNonCommentSibling(@Nullable PsiElement element) {
        return PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace.class, PsiComment.class);
    }

    /**
     * Finds first sibling that is neither comment, nor whitespace after given element.
     */
    public static @Nullable PsiElement getNextNonCommentSibling(@Nullable PsiElement element) {
        return PsiTreeUtil.skipSiblingsForward(element, PsiWhiteSpace.class, PsiComment.class);
    }

    /**
     * Kotlin's {@code generateSequence(seed, nextFunction)}: the elements up to the first {@code null}.
     */
    private static Stream<PsiElement> generate(@Nullable PsiElement seed, NextFunction next) {
        if (seed == null) {
            return Stream.empty();
        }
        return Stream.iterate(seed, Objects::nonNull, next::next);
    }

    @FunctionalInterface
    private interface NextFunction {
        @Nullable PsiElement next(PsiElement element);
    }
}

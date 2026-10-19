package io.github.intellij.dlanguage.psi.interfaces;

import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface Expression extends PsiElement {

    /**
     * Returns the type of expression.
     *
     * @return the expression type or null if it is not known.
     */
    @Contract(pure = true)
    @Nullable DType getDType();
}

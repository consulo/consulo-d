package io.github.intellij.dlanguage.psi.interfaces;

import io.github.intellij.dlanguage.psi.types.DType;
import org.jetbrains.annotations.Contract;

public interface DTypedElement {

    @Contract(pure = true)
    DType getDType();
}

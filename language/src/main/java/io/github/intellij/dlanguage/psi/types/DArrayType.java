package io.github.intellij.dlanguage.psi.types;

import org.jspecify.annotations.Nullable;

public class DArrayType implements DType {
    private final DType myBase;
    private final @Nullable Long mySize;

    public DArrayType(DType base, @Nullable Long size) {
        myBase = base;
        mySize = size;
    }

    public DType getBase() {
        return myBase;
    }

    public @Nullable Long getSize() {
        return mySize;
    }

    @Override
    public String toString() {
        return mySize == null ? myBase + "[]" : myBase + "[" + mySize + "]";
    }

    public boolean isFixedSize() {
        return mySize != null;
    }
}

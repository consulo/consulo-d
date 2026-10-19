package io.github.intellij.dlanguage.psi.types;

public class DPointerType implements DType {
    private final DType myBase;

    public DPointerType(DType base) {
        myBase = base;
    }

    public DType getBase() {
        return myBase;
    }

    @Override
    public String toString() {
        return myBase + "*";
    }
}

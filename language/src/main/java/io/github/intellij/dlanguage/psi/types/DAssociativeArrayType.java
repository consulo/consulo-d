package io.github.intellij.dlanguage.psi.types;

/**
 * Declared in upstream {@code DArrayType.kt}.
 */
public class DAssociativeArrayType implements DType {
    private final DType myValueType;
    private final DType myKeyType;

    public DAssociativeArrayType(DType valueType, DType keyType) {
        myValueType = valueType;
        myKeyType = keyType;
    }

    public DType getValueType() {
        return myValueType;
    }

    public DType getKeyType() {
        return myKeyType;
    }

    @Override
    public String toString() {
        return myValueType + "[" + myKeyType + "]";
    }
}

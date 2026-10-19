package io.github.intellij.dlanguage.psi.types;

public class DAliasType implements DType {
    private final String myName;
    private final DType myAliased;

    public DAliasType(String name, DType aliased) {
        myName = name;
        myAliased = aliased;
    }

    public String getName() {
        return myName;
    }

    public DType getAliased() {
        return myAliased;
    }

    @Override
    public String toString() {
        return "alias " + myName + " = " + myAliased;
    }
}

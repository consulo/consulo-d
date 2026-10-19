package io.github.intellij.dlanguage.psi.types;

import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;

public class UserDefinedDType implements DType {
    private final String myName;
    private final UserDefinedType myInstance;

    public UserDefinedDType(String name, UserDefinedType instance) {
        myName = name;
        myInstance = instance;
    }

    public String getName() {
        return myName;
    }

    @Override
    public String toString() {
        return myName;
    }

    public UserDefinedType resolve() {
        return myInstance;
    }
}

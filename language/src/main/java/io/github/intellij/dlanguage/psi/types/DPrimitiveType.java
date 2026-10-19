package io.github.intellij.dlanguage.psi.types;

public class DPrimitiveType implements DType {
    public static final DPrimitiveType BOOL = new DPrimitiveType("bool");
    public static final DPrimitiveType BYTE = new DPrimitiveType("byte");
    public static final DPrimitiveType UBYTE = new DPrimitiveType("ubyte");
    public static final DPrimitiveType SHORT = new DPrimitiveType("short");
    public static final DPrimitiveType USHORT = new DPrimitiveType("ushort");
    public static final DPrimitiveType INT = new DPrimitiveType("int");
    public static final DPrimitiveType UINT = new DPrimitiveType("uint");
    public static final DPrimitiveType LONG = new DPrimitiveType("long");
    public static final DPrimitiveType ULONG = new DPrimitiveType("ulong");
    public static final DPrimitiveType FLOAT = new DPrimitiveType("float");
    public static final DPrimitiveType DOUBLE = new DPrimitiveType("double");
    public static final DPrimitiveType CHAR = new DPrimitiveType("char");
    public static final DPrimitiveType WCHAR = new DPrimitiveType("wchar");
    public static final DPrimitiveType DCHAR = new DPrimitiveType("dchar");
    public static final DPrimitiveType VOID = new DPrimitiveType("void");

    public static final DPrimitiveType REAL = new DPrimitiveType("ireal");
    public static final DPrimitiveType CENT = new DPrimitiveType("cent");
    public static final DPrimitiveType UCENT = new DPrimitiveType("ucent");
    public static final DPrimitiveType CFLOAT = new DPrimitiveType("cfloat");
    public static final DPrimitiveType CDOUBLE = new DPrimitiveType("cdouble");
    public static final DPrimitiveType CREAL = new DPrimitiveType("creal");
    public static final DPrimitiveType IFLOAT = new DPrimitiveType("ifloat");
    public static final DPrimitiveType IDOUBLE = new DPrimitiveType("idouble");
    public static final DPrimitiveType IREAL = new DPrimitiveType("ireal");

    // not existing actually but represent a null pointer
    private static final DPrimitiveType NULL = new DPrimitiveType("null");

    private final String myName;

    private DPrimitiveType(String name) {
        myName = name;
    }

    public String getName() {
        return myName;
    }

    @Override
    public String toString() {
        return myName;
    }

    public static DPrimitiveType fromText(String text) {
        return switch (text) {
            case "bool" -> BOOL;
            case "byte" -> BYTE;
            case "ubyte" -> UBYTE;
            case "short" -> SHORT;
            case "ushort" -> USHORT;
            case "int" -> INT;
            case "uint" -> UINT;
            case "long" -> LONG;
            case "ulong" -> ULONG;
            case "float" -> FLOAT;
            case "double" -> DOUBLE;
            case "char" -> CHAR;
            case "wchar" -> WCHAR;
            case "dchar" -> DCHAR;
            case "void" -> VOID;
            case "null" -> NULL;

            case "real" -> REAL;
            case "cent" -> CENT;
            case "ucent" -> UCENT;
            case "cfloat" -> CFLOAT;
            case "cdouble" -> CDOUBLE;
            case "ifloat" -> IFLOAT;
            case "idouble" -> IDOUBLE;
            case "creal" -> CREAL;
            case "ireal" -> IREAL;

            // upstream throws Kotlin's NotImplementedError
            default -> throw new UnsupportedOperationException("Missing implementation for type " + text);
        };
    }
}

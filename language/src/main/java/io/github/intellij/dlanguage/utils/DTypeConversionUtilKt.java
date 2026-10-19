package io.github.intellij.dlanguage.utils;

import io.github.intellij.dlanguage.psi.types.DAliasType;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Top-level functions of upstream {@code DTypeConversionUtil.kt}.
 */
public final class DTypeConversionUtilKt {
    private DTypeConversionUtilKt() {
    }

    public static @Nullable DType getCommonType(@Nullable DType type1, @Nullable DType type2) {
        if (type1 == null) {
            return type2;
        }
        if (type2 == null) {
            return type1;
        }
        DType unboxedType1 = Objects.requireNonNull(getUnaliasedType(type1));
        DType unboxedType2 = Objects.requireNonNull(getUnaliasedType(type2));

        if (isNumericType(unboxedType1) && isNumericType(unboxedType2)) {
            return getNumericTypeCommon(unboxedType1, unboxedType2);
        }
        if (isCharType(unboxedType1) && isCharType(unboxedType2)) {
            return getCharTypeCommon(unboxedType1, unboxedType2);
        }
        // TODO handle other cases, class common parent, pointers, ...
        return type1;
    }


    public static boolean isNumericType(DType type) {
        return List.of(DPrimitiveType.DOUBLE, DPrimitiveType.FLOAT, DPrimitiveType.LONG,
            DPrimitiveType.ULONG, DPrimitiveType.INT, DPrimitiveType.UINT, DPrimitiveType.SHORT,
            DPrimitiveType.USHORT, DPrimitiveType.BYTE, DPrimitiveType.UBYTE).contains(type);
    }

    private static @Nullable DType getNumericTypeCommon(DType type1, DType type2) {
        if (type1 == DPrimitiveType.DOUBLE || type2 == DPrimitiveType.DOUBLE) {
            return DPrimitiveType.DOUBLE;
        }
        if (type1 == DPrimitiveType.FLOAT || type2 == DPrimitiveType.FLOAT) {
            return DPrimitiveType.FLOAT;
        }
        if (type1 == DPrimitiveType.LONG || type2 == DPrimitiveType.LONG) {
            return DPrimitiveType.LONG;
        }
        if (type1 == DPrimitiveType.ULONG || type2 == DPrimitiveType.ULONG) {
            return DPrimitiveType.ULONG;
        }
        if (type1 == DPrimitiveType.INT || type2 == DPrimitiveType.INT) {
            return DPrimitiveType.INT;
        }
        if (type1 == DPrimitiveType.UINT || type2 == DPrimitiveType.UINT) {
            return DPrimitiveType.UINT;
        }
        if (type1 == DPrimitiveType.SHORT || type2 == DPrimitiveType.SHORT) {
            return DPrimitiveType.SHORT;
        }
        if (type1 == DPrimitiveType.USHORT || type2 == DPrimitiveType.USHORT) {
            return DPrimitiveType.USHORT;
        }
        if (type1 == DPrimitiveType.BYTE || type2 == DPrimitiveType.BYTE) {
            return DPrimitiveType.BYTE;
        }
        if (type1 == DPrimitiveType.UBYTE || type2 == DPrimitiveType.UBYTE) {
            return DPrimitiveType.UBYTE;
        }
        return null;
    }

    private static boolean isCharType(DType type) {
        return type == DPrimitiveType.CHAR || type == DPrimitiveType.WCHAR || type == DPrimitiveType.DCHAR;
    }

    private static @Nullable DType getCharTypeCommon(DType type1, @Nullable DType type2) {
        if (type1 == DPrimitiveType.UBYTE || type2 == DPrimitiveType.UBYTE) {
            return DPrimitiveType.UBYTE;
        }
        if (type1 == DPrimitiveType.UBYTE || type2 == DPrimitiveType.UBYTE) {
            return DPrimitiveType.UBYTE;
        }
        if (type1 == DPrimitiveType.UBYTE || type2 == DPrimitiveType.UBYTE) {
            return DPrimitiveType.UBYTE;
        }
        return null;
    }

    public static @Nullable DType getUnaliasedType(@Nullable DType type) {
        if (type == null) {
            return null;
        }
        DType unboxedType = type;
        while (unboxedType instanceof DAliasType aliasType) {
            unboxedType = aliasType.getAliased();
        }
        return unboxedType;
    }
}

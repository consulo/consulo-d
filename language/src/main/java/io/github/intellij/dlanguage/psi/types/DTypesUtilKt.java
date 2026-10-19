package io.github.intellij.dlanguage.psi.types;

import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DLanguageTypeSuffix;

import java.util.List;

/**
 * Top-level functions of upstream {@code DTypesUtil.kt}.
 */
public final class DTypesUtilKt {
    private DTypesUtilKt() {
    }

    public static DType getDtypeWithSuffixes(DType elementType, List<DLanguageTypeSuffix> suffixes) {
        DType dtype = elementType;
        for (DLanguageTypeSuffix suffix : suffixes) {
            if (suffix.getOP_ASTERISK() != null) {
                dtype = new DPointerType(dtype);
            }
            else if (suffix.getOP_BRACKET_LEFT() != null) {
                DLanguageType type = suffix.getType();
                if (type != null) {
                    dtype = new DAssociativeArrayType(
                        dtype,
                        type.getDType()
                    );
                }
                else if (suffix.getExpressions().isEmpty()) {
                    dtype = new DArrayType(dtype, null);
                }
                else {
                    dtype = new DArrayType(dtype, null); // TODO use expression value to set length
                }
            }
            else if (suffix.getOP_TRIPLEDOT() != null) {
                dtype = new DArrayType(dtype, null);
            }
            else if (suffix.getKW_DELEGATE() != null) {
                // TODO implement
            }
            else if (suffix.getKW_FUNCTION() != null) {
                // TODO implement
            }
        }
        return dtype;
    }
}

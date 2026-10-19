package io.github.intellij.dlanguage.resolve;

import consulo.util.dataholder.Key;

import java.util.List;

/**
 * Top-level properties of upstream {@code DResolveUtil.kt}.
 */
public final class DResolveUtilKt {
    private static final Key<List<String>> PROCESSED_FILES_KEY = Key.create("PROCESSED_FILES");
    private static final Key<Boolean> IS_QUALIFIED_SYMBOL = Key.create("IS_QUALIFIED_SYMBOL");

    private DResolveUtilKt() {
    }

    public static Key<List<String>> getPROCESSED_FILES_KEY() {
        return PROCESSED_FILES_KEY;
    }

    public static Key<Boolean> getIS_QUALIFIED_SYMBOL() {
        return IS_QUALIFIED_SYMBOL;
    }
}

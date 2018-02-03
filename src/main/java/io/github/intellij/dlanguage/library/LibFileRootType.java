package io.github.intellij.dlanguage.library;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.roots.OrderRootType;

public class LibFileRootType extends OrderRootType
{
    protected LibFileRootType() {
        super("dLibraries");
    }

    @NotNull
    public static LibFileRootType getInstance() {
        return getOrderRootType(LibFileRootType.class);
    }
}

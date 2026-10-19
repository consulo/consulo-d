package io.github.intellij.dlanguage.library;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.content.OrderRootType;

@ExtensionImpl
public class LibFileRootType extends OrderRootType {
    public static final String ID = "LIBRARY_FILE";

    public LibFileRootType() {
        super(ID);
    }

    public static LibFileRootType getInstance() {
        return Application.get().getExtensionPoint(OrderRootType.class).findExtensionOrFail(LibFileRootType.class);
    }
}

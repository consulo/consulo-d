package io.github.intellij.dlanguage.folding;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.application.Application;

@ServiceAPI(ComponentScope.APPLICATION)
public abstract class DCodeFoldingSettings {
    public abstract boolean isCollapsibleOneLineMethods();

    public abstract void setCollapsibleOneLineMethods(boolean collapsibleOneLineMethods);

    public static DCodeFoldingSettings getInstance() {
        return Application.get().getInstance(DCodeFoldingSettings.class);
    }
}

package io.github.intellij.dlanguage.folding.impl;

import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.RoamingType;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.util.xml.serializer.XmlSerializerUtil;
import io.github.intellij.dlanguage.folding.DCodeFoldingSettings;
import jakarta.inject.Singleton;

@Singleton
@ServiceImpl
@State(name = "DCodeFoldingSettings",
    storages = @Storage(value = "editor.codeinsight.xml", roamingType = RoamingType.DISABLED)
)
public class DCodeFoldingSettingsImpl extends DCodeFoldingSettings implements PersistentStateComponent<DCodeFoldingSettingsImpl> {

    private boolean myCollapsibleOneLineMethods = true;

    @Override
    public boolean isCollapsibleOneLineMethods() {
        return myCollapsibleOneLineMethods;
    }

    @Override
    public void setCollapsibleOneLineMethods(boolean collapsibleOneLineMethods) {
        myCollapsibleOneLineMethods = collapsibleOneLineMethods;
    }

    @Override
    public DCodeFoldingSettingsImpl getState() {
        return this;
    }

    @Override
    public void loadState(DCodeFoldingSettingsImpl state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

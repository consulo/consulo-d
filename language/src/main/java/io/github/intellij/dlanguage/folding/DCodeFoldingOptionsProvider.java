package io.github.intellij.dlanguage.folding;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.SimpleConfigurableByProperties;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.jspecify.annotations.Nullable;

/**
 * Upstream's {@code CodeFoldingOptionsProvider}; in Consulo the D section of Editor > Code Folding is a configurable.
 */
@ExtensionImpl
public class DCodeFoldingOptionsProvider extends SimpleConfigurableByProperties implements ApplicationConfigurable {
    private final Provider<DCodeFoldingSettings> mySettingsProvider;

    @Inject
    public DCodeFoldingOptionsProvider(Provider<DCodeFoldingSettings> settingsProvider) {
        mySettingsProvider = settingsProvider;
    }

    @Override
    public String getId() {
        return "editor.preferences.folding.d";
    }

    @Override
    public @Nullable String getParentId() {
        return "editor.preferences.folding";
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.moduleExtensionName();
    }

    @RequiredUIAccess
    @Override
    protected Component createLayout(PropertyBuilder propertyBuilder, Disposable uiDisposable) {
        VerticalLayout layout = VerticalLayout.create();
        DCodeFoldingSettings settings = mySettingsProvider.get();
        CheckBox checkBox = CheckBox.create(DLocalize.foldingCollapsibleOneLineMethods());
        propertyBuilder.add(checkBox, settings::isCollapsibleOneLineMethods, settings::setCollapsibleOneLineMethods);
        layout.add(checkBox);
        return layout;
    }
}

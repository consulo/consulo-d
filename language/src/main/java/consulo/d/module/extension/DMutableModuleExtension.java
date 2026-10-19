package consulo.d.module.extension;

import consulo.content.bundle.Sdk;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;

import java.util.List;

public class DMutableModuleExtension extends DModuleExtension implements MutableModuleExtensionWithSdk<DModuleExtension> {
    public DMutableModuleExtension(String id, ModuleRootLayer moduleRootLayer) {
        super(id, moduleRootLayer);
    }

    @Override
    public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
        return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
    }

    public void setVersionIdentifiers(List<String> identifiers) {
        myVersionIdentifiers.clear();
        myVersionIdentifiers.addAll(identifiers);
    }

    public void setDebugIdentifiers(List<String> identifiers) {
        myDebugIdentifiers.clear();
        myDebugIdentifiers.addAll(identifiers);
    }

    @Override
    public void setEnabled(boolean enabled) {
        myIsEnabled = enabled;
    }

    @Override
    public boolean isModified(DModuleExtension originalExtension) {
        return isModifiedImpl(originalExtension)
            || !myVersionIdentifiers.equals(originalExtension.myVersionIdentifiers)
            || !myDebugIdentifiers.equals(originalExtension.myDebugIdentifiers);
    }

    @RequiredUIAccess
    @Override
    public Component createConfigurationComponent(Disposable uiDisposable, Runnable updateOnCheck) {
        VerticalLayout layout = VerticalLayout.create();
        layout.add(ModuleExtensionBundleBoxBuilder.createAndDefine(this, uiDisposable, updateOnCheck).build());

        layout.add(Label.create(DLocalize.moduleExtensionVersionIdentifiers()));
        TextBox versionsBox = TextBox.create(formatIdentifiers(myVersionIdentifiers));
        versionsBox.addValueListener(event -> {
            setVersionIdentifiers(parseIdentifiers(event.getValue()));
            updateOnCheck.run();
        });
        layout.add(versionsBox);

        layout.add(Label.create(DLocalize.moduleExtensionDebugIdentifiers()));
        TextBox debugBox = TextBox.create(formatIdentifiers(myDebugIdentifiers));
        debugBox.addValueListener(event -> {
            setDebugIdentifiers(parseIdentifiers(event.getValue()));
            updateOnCheck.run();
        });
        layout.add(debugBox);
        return layout;
    }
}

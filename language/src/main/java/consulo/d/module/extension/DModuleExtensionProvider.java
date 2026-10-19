package consulo.d.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class DModuleExtensionProvider implements ModuleExtensionProvider<DModuleExtension> {
    public static final String ID = "d";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.moduleExtensionName();
    }

    @Override
    public Image getIcon() {
        return DLanguage.Icons.MODULE;
    }

    @Override
    public ModuleExtension<DModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new DModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<DModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new DMutableModuleExtension(getId(), moduleRootLayer);
    }
}

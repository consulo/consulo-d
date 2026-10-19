package consulo.d.dub.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.dub.DubConstants;
import consulo.d.localize.DLocalize;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import consulo.d.icon.DIconGroup;
import org.jspecify.annotations.Nullable;

/**
 * Marks a module as imported from DUB.
 */
@ExtensionImpl
public class DubModuleExtensionProvider implements ModuleExtensionProvider<ExternalSystemModuleExtensionImpl> {
    @Override
    public String getId() {
        return DubConstants.SYSTEM_ID.getId();
    }

    @Override
    public @Nullable String getParentId() {
        return null;
    }

    @Override
    public boolean isSystemOnly() {
        return true;
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.dubSystemName();
    }

    @Override
    public Image getIcon() {
        return DIconGroup.dubBall();
    }

    @Override
    public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemModuleExtensionImpl(getId(), moduleRootLayer, DubConstants.SYSTEM_ID);
    }

    @Override
    public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemMutableModuleExtensionImpl(getId(), moduleRootLayer, DubConstants.SYSTEM_ID);
    }
}

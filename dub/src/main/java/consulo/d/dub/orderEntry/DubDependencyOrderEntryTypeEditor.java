package consulo.d.dub.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.ide.setting.module.CustomOrderEntryTypeEditor;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.ColoredTextContainer;
import consulo.ui.ex.SimpleTextAttributes;

import java.util.function.Consumer;

/**
 * Shows a DUB package dependency as its name and version, in red when its directory is gone.
 */
@ExtensionImpl
public class DubDependencyOrderEntryTypeEditor implements CustomOrderEntryTypeEditor<DubDependencyOrderEntryModel> {
    @Override
    public Consumer<ColoredTextContainer> getRenderer(CustomOrderEntry<DubDependencyOrderEntryModel> orderEntry,
                                                      DubDependencyOrderEntryModel model) {
        return container -> {
            container.setIcon(PlatformIconGroup.nodesPplib());
            container.append(model.getName(), model.findDirectory() != null ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.ERROR_ATTRIBUTES);
            if (!model.getVersion().isEmpty()) {
                container.append(" " + model.getVersion(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        };
    }

    @Override
    public String getOrderTypeId() {
        return DubDependencyOrderEntryType.ID;
    }
}

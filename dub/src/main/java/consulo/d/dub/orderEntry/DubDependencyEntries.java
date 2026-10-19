package consulo.d.dub.orderEntry;

import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DubDependencyEntries {
    private DubDependencyEntries() {
    }

    /**
     * Swaps the DUB packages of a module for {@code models}, keeping the entries that did not change so an unchanged
     * dependency tree does not touch the root model.
     */
    public static void replace(ModifiableRootModel rootModel, List<DubDependencyOrderEntryModel> models) {
        List<DubDependencyOrderEntryModel> existing = new ArrayList<>();
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
            DubDependencyOrderEntryModel model = getModel(orderEntry);
            if (model != null) {
                existing.add(model);
            }
        }
        if (isSame(existing, models)) {
            return;
        }
        removeAll(rootModel);
        DubDependencyOrderEntryType type = DubDependencyOrderEntryType.getInstance();
        for (DubDependencyOrderEntryModel model : models) {
            rootModel.addCustomOderEntry(type, model);
        }
    }

    public static void removeAll(ModifiableRootModel rootModel) {
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
            if (getModel(orderEntry) != null) {
                rootModel.removeOrderEntry(orderEntry);
            }
        }
    }

    public static @Nullable DubDependencyOrderEntryModel getModel(OrderEntry orderEntry) {
        return orderEntry instanceof CustomOrderEntry<?> customOrderEntry
            && customOrderEntry.getModel() instanceof DubDependencyOrderEntryModel model ? model : null;
    }

    private static boolean isSame(List<DubDependencyOrderEntryModel> existing, List<DubDependencyOrderEntryModel> models) {
        if (existing.size() != models.size()) {
            return false;
        }
        for (int i = 0; i < existing.size(); i++) {
            if (!existing.get(i).isEquivalentTo(models.get(i))) {
                return false;
            }
        }
        return true;
    }
}

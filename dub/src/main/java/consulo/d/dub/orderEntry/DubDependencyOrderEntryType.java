package consulo.d.dub.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryTypeProvider;
import consulo.util.xml.serializer.InvalidDataException;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ExtensionImpl
public class DubDependencyOrderEntryType implements CustomOrderEntryTypeProvider<DubDependencyOrderEntryModel> {
    public static final String ID = "dub-package";

    private static final String NAME_ATTRIBUTE = "name";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String DIRECTORY_ATTRIBUTE = "directory";
    private static final String ROOT_ELEMENT = "root";
    private static final String URL_ATTRIBUTE = "url";
    private static final String VERSION_ELEMENT = "version";
    private static final String DEBUG_ELEMENT = "debug";

    public static DubDependencyOrderEntryType getInstance() {
        return EP.findExtensionOrFail(Application.get(), DubDependencyOrderEntryType.class);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public DubDependencyOrderEntryModel loadOrderEntry(Element element, ModuleRootLayer moduleRootLayer) throws InvalidDataException {
        String name = element.getAttributeValue(NAME_ATTRIBUTE);
        if (name == null) {
            throw new InvalidDataException("DUB package dependency without a name");
        }
        String version = element.getAttributeValue(VERSION_ATTRIBUTE);
        return new DubDependencyOrderEntryModel(name,
            version == null ? "" : version,
            element.getAttributeValue(DIRECTORY_ATTRIBUTE),
            readChildren(element, ROOT_ELEMENT, URL_ATTRIBUTE),
            readChildren(element, VERSION_ELEMENT, NAME_ATTRIBUTE),
            readChildren(element, DEBUG_ELEMENT, NAME_ATTRIBUTE));
    }

    @Override
    public void storeOrderEntry(Element element, DubDependencyOrderEntryModel model) {
        element.setAttribute(NAME_ATTRIBUTE, model.getName());
        if (!model.getVersion().isEmpty()) {
            element.setAttribute(VERSION_ATTRIBUTE, model.getVersion());
        }
        String directoryUrl = model.getDirectoryUrl();
        if (directoryUrl != null) {
            element.setAttribute(DIRECTORY_ATTRIBUTE, directoryUrl);
        }
        writeChildren(element, ROOT_ELEMENT, URL_ATTRIBUTE, model.getSourceRootUrls());
        writeChildren(element, VERSION_ELEMENT, NAME_ATTRIBUTE, model.getVersionIdentifiers());
        writeChildren(element, DEBUG_ELEMENT, NAME_ATTRIBUTE, model.getDebugIdentifiers());
    }

    private static List<String> readChildren(Element element, String childName, String attribute) {
        List<String> values = new ArrayList<>();
        for (Element child : element.getChildren(childName)) {
            @Nullable String value = child.getAttributeValue(attribute);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private static void writeChildren(Element element, String childName, String attribute, List<String> values) {
        for (String value : values) {
            element.addContent(new Element(childName).setAttribute(attribute, value));
        }
    }
}

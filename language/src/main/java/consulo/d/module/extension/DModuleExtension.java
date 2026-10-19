package consulo.d.module.extension;

import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.SdkType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;
import io.github.intellij.dlanguage.DlangSdkType;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Marks a module as D: the D compiler it is built with, and the {@code version} / {@code debug} identifiers it is
 * compiled with ({@code -version=X}, {@code -debug=X}).
 */
public class DModuleExtension extends ModuleExtensionWithSdkBase<DModuleExtension> {
    private static final String VERSION_ELEMENT = "version";
    private static final String DEBUG_ELEMENT = "debug";
    private static final String NAME_ATTRIBUTE = "name";

    protected final List<String> myVersionIdentifiers = new ArrayList<>();
    protected final List<String> myDebugIdentifiers = new ArrayList<>();

    public DModuleExtension(String id, ModuleRootLayer moduleRootLayer) {
        super(id, moduleRootLayer);
    }

    public List<String> getVersionIdentifiers() {
        return List.copyOf(myVersionIdentifiers);
    }

    public List<String> getDebugIdentifiers() {
        return List.copyOf(myDebugIdentifiers);
    }

    @Override
    public Class<? extends SdkType> getSdkTypeClass() {
        return DlangSdkType.class;
    }

    @RequiredReadAction
    @Override
    public void commit(DModuleExtension mutableModuleExtension) {
        super.commit(mutableModuleExtension);
        myVersionIdentifiers.clear();
        myVersionIdentifiers.addAll(mutableModuleExtension.myVersionIdentifiers);
        myDebugIdentifiers.clear();
        myDebugIdentifiers.addAll(mutableModuleExtension.myDebugIdentifiers);
    }

    @Override
    protected void getStateImpl(Element element) {
        super.getStateImpl(element);
        writeIdentifiers(element, VERSION_ELEMENT, myVersionIdentifiers);
        writeIdentifiers(element, DEBUG_ELEMENT, myDebugIdentifiers);
    }

    @RequiredReadAction
    @Override
    protected void loadStateImpl(Element element) {
        super.loadStateImpl(element);
        readIdentifiers(element, VERSION_ELEMENT, myVersionIdentifiers);
        readIdentifiers(element, DEBUG_ELEMENT, myDebugIdentifiers);
    }

    private static void writeIdentifiers(Element element, String childName, List<String> identifiers) {
        for (String identifier : identifiers) {
            element.addContent(new Element(childName).setAttribute(NAME_ATTRIBUTE, identifier));
        }
    }

    private static void readIdentifiers(Element element, String childName, List<String> identifiers) {
        identifiers.clear();
        for (Element child : element.getChildren(childName)) {
            String name = child.getAttributeValue(NAME_ATTRIBUTE);
            if (name != null && !name.isBlank()) {
                identifiers.add(name.trim());
            }
        }
    }

    /**
     * {@code X; Y} as typed in the module settings.
     */
    public static List<String> parseIdentifiers(String text) {
        List<String> result = new ArrayList<>();
        for (String part : text.split(";")) {
            String identifier = part.trim();
            if (!identifier.isEmpty() && !result.contains(identifier)) {
                result.add(identifier);
            }
        }
        return result;
    }

    public static String formatIdentifiers(List<String> identifiers) {
        return String.join("; ", identifiers);
    }
}

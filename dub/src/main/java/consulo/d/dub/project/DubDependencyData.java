package consulo.d.dub.project;

import consulo.d.dub.DubConstants;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * One DUB package the module depends on, as {@code dub describe} reports it.
 */
public class DubDependencyData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<DubDependencyData> KEY = Key.create(DubDependencyData.class, ProjectKeys.LIBRARY_DEPENDENCY.getProcessingWeight() + 1);

    private final String myName;
    private final String myVersion;
    private final String myDirectory;
    private final List<String> myImportPaths;
    private final List<String> myVersionIdentifiers;
    private final List<String> myDebugIdentifiers;

    public DubDependencyData(String name,
                             String version,
                             String directory,
                             List<String> importPaths,
                             List<String> versionIdentifiers,
                             List<String> debugIdentifiers) {
        super(DubConstants.SYSTEM_ID);
        myName = name;
        myVersion = version;
        myDirectory = directory;
        myImportPaths = List.copyOf(importPaths);
        myVersionIdentifiers = List.copyOf(versionIdentifiers);
        myDebugIdentifiers = List.copyOf(debugIdentifiers);
    }

    public String getName() {
        return myName;
    }

    public String getVersion() {
        return myVersion;
    }

    public String getDirectory() {
        return myDirectory;
    }

    /**
     * @return absolute import paths of the package
     */
    public List<String> getImportPaths() {
        return myImportPaths;
    }

    public List<String> getVersionIdentifiers() {
        return myVersionIdentifiers;
    }

    public List<String> getDebugIdentifiers() {
        return myDebugIdentifiers;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof DubDependencyData that
            && super.equals(o)
            && myName.equals(that.myName)
            && myVersion.equals(that.myVersion)
            && myDirectory.equals(that.myDirectory)
            && myImportPaths.equals(that.myImportPaths)
            && myVersionIdentifiers.equals(that.myVersionIdentifiers)
            && myDebugIdentifiers.equals(that.myDebugIdentifiers);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(myName, myVersion, myDirectory, myImportPaths, myVersionIdentifiers, myDebugIdentifiers);
    }
}

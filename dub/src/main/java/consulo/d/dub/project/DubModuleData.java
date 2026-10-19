package consulo.d.dub.project;

import consulo.d.dub.DubConstants;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * What a module compiles with, as DUB reports it: the version identifiers (predefined and the package's own) and the
 * debug identifiers.
 */
public class DubModuleData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    /**
     * Processed after the module and its content roots exist.
     */
    public static final Key<DubModuleData> KEY = Key.create(DubModuleData.class, ProjectKeys.CONTENT_ROOT.getProcessingWeight() + 1);

    private final List<String> myVersionIdentifiers;
    private final List<String> myDebugIdentifiers;
    private final @Nullable String myTargetExecutable;

    public DubModuleData(List<String> versionIdentifiers, List<String> debugIdentifiers, @Nullable String targetExecutable) {
        super(DubConstants.SYSTEM_ID);
        myVersionIdentifiers = List.copyOf(versionIdentifiers);
        myDebugIdentifiers = List.copyOf(debugIdentifiers);
        myTargetExecutable = targetExecutable;
    }

    /**
     * @return the file the root package builds to, {@code null} for a package that builds no file
     */
    public @Nullable String getTargetExecutable() {
        return myTargetExecutable;
    }

    public List<String> getVersionIdentifiers() {
        return myVersionIdentifiers;
    }

    public List<String> getDebugIdentifiers() {
        return myDebugIdentifiers;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof DubModuleData that
            && super.equals(o)
            && myVersionIdentifiers.equals(that.myVersionIdentifiers)
            && myDebugIdentifiers.equals(that.myDebugIdentifiers)
            && Objects.equals(myTargetExecutable, that.myTargetExecutable);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(myVersionIdentifiers, myDebugIdentifiers, myTargetExecutable);
    }
}

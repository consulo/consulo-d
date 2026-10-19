package consulo.d.dub.orderEntry;

import consulo.content.RootProvider;
import consulo.d.language.moduleAware.DCompileOptions;
import consulo.d.language.moduleAware.DCompileOptionsOwner;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A DUB package of the dependency tree: its sources live in the DUB package cache or wherever a path dependency
 * points. The entry carries the import paths and the version / debug identifiers DUB reported for the package.
 */
public class DubDependencyOrderEntryModel implements CustomOrderEntryModel, DCompileOptionsOwner {
    private final String myName;
    private final String myVersion;
    private final @Nullable String myDirectoryUrl;
    private final List<String> mySourceRootUrls;
    private final List<String> myVersionIdentifiers;
    private final List<String> myDebugIdentifiers;
    private final DubDependencyRootProvider myRootProvider = new DubDependencyRootProvider(this);

    public DubDependencyOrderEntryModel(String name,
                                        String version,
                                        @Nullable String directoryUrl,
                                        List<String> sourceRootUrls,
                                        List<String> versionIdentifiers,
                                        List<String> debugIdentifiers) {
        myName = name;
        myVersion = version;
        myDirectoryUrl = directoryUrl;
        mySourceRootUrls = List.copyOf(sourceRootUrls);
        myVersionIdentifiers = List.copyOf(versionIdentifiers);
        myDebugIdentifiers = List.copyOf(debugIdentifiers);
    }

    public String getName() {
        return myName;
    }

    public String getVersion() {
        return myVersion;
    }

    public @Nullable String getDirectoryUrl() {
        return myDirectoryUrl;
    }

    public List<String> getSourceRootUrls() {
        return mySourceRootUrls;
    }

    public List<String> getVersionIdentifiers() {
        return myVersionIdentifiers;
    }

    public List<String> getDebugIdentifiers() {
        return myDebugIdentifiers;
    }

    @Override
    public @Nullable DCompileOptions getCompileOptions() {
        DCompileOptions options = new DCompileOptions(myVersionIdentifiers, myDebugIdentifiers);
        return options.isEmpty() ? null : options;
    }

    @Override
    public void bind(ModuleRootLayer moduleRootLayer) {
    }

    @Override
    public String getPresentableName() {
        return myVersion.isEmpty() ? myName : myName + " " + myVersion;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isSynthetic() {
        return true;
    }

    @Override
    public RootProvider getRootProvider() {
        return myRootProvider;
    }

    @Override
    public VirtualFile[] getExcludedRoots() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public Object getEqualObject() {
        return DubDependencyOrderEntryType.ID + " " + myName + " " + myVersion;
    }

    @Override
    public boolean isEquivalentTo(CustomOrderEntryModel model) {
        return model instanceof DubDependencyOrderEntryModel other
            && myName.equals(other.myName)
            && myVersion.equals(other.myVersion)
            && Objects.equals(myDirectoryUrl, other.myDirectoryUrl)
            && mySourceRootUrls.equals(other.mySourceRootUrls)
            && myVersionIdentifiers.equals(other.myVersionIdentifiers)
            && myDebugIdentifiers.equals(other.myDebugIdentifiers);
    }

    @Override
    public DubDependencyOrderEntryModel clone() {
        return new DubDependencyOrderEntryModel(myName, myVersion, myDirectoryUrl, mySourceRootUrls, myVersionIdentifiers, myDebugIdentifiers);
    }
}

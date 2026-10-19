package consulo.d.dub.orderEntry;

import consulo.content.RootProviderBase;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Serves the import paths of the package both as sources and as binaries, the same way the D SDK serves phobos and
 * druntime.
 */
class DubDependencyRootProvider extends RootProviderBase {
    private final DubDependencyOrderEntryModel myModel;

    DubDependencyRootProvider(DubDependencyOrderEntryModel model) {
        myModel = model;
    }

    @Override
    public String[] getUrls(String rootType) {
        return isServed(rootType) ? myModel.getSourceRootUrls().toArray(String[]::new) : new String[0];
    }

    @Override
    public VirtualFile[] getFiles(String rootType) {
        if (!isServed(rootType)) {
            return VirtualFile.EMPTY_ARRAY;
        }
        List<VirtualFile> files = new ArrayList<>();
        for (String url : myModel.getSourceRootUrls()) {
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
            if (file != null && file.isDirectory()) {
                files.add(file);
            }
        }
        return files.toArray(VirtualFile.EMPTY_ARRAY);
    }

    private static boolean isServed(String rootType) {
        return SourcesOrderRootType.ID.equals(rootType) || BinariesOrderRootType.ID.equals(rootType);
    }
}

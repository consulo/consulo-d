package io.github.intellij.dlanguage.library;

import consulo.application.progress.ProgressIndicator;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.library.ui.RootDetector;
import consulo.localize.LocalizeValue;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import io.github.intellij.dlanguage.DlangFileType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DlangLibRootDetector extends RootDetector {
    protected DlangLibRootDetector(final String rootType, final LocalizeValue presentableRootTypeName) {
        super(rootType, false, presentableRootTypeName);
    }

    private static boolean containsDLangFiles(final VirtualFile dir) {
        final VirtualFileVisitor.Result result = VirtualFileUtil.visitChildrenRecursively(dir, new VirtualFileVisitor() {
            @Override
            public Result visitFileEx(final VirtualFile file) {
                return !file.isDirectory() && DlangFileType.DEFAULT_EXTENSION.equalsIgnoreCase(file.getExtension()) ? skipTo(dir) : CONTINUE;
            }
        });
        return result.skipToParent != null;
    }

    @Override
    public Collection<VirtualFile> detectRoots(final VirtualFile rootCandidate, final ProgressIndicator progressIndicator) {
        final List<VirtualFile> result = new ArrayList<VirtualFile>();
        collectRoots(rootCandidate, result, progressIndicator);
        return result;
    }

    public void collectRoots(final VirtualFile file, final List<VirtualFile> result, @Nullable final ProgressIndicator progressIndicator) {
        if (file.getFileSystem() instanceof ArchiveFileSystem) {
            return;
        }

        final String rootType = getRootType();

        VirtualFileUtil.visitChildrenRecursively(file, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(final VirtualFile file) {
                if (progressIndicator != null) {
                    progressIndicator.checkCanceled();
                }
                if (!file.isDirectory()) {
                    if (rootType.equals(LibFileRootType.ID) && "lib".equals(file.getExtension())) {
                        result.add(file);
                    }
                    return false;
                }

                if (progressIndicator != null) {
                    progressIndicator.setText2(LocalizeValue.of(file.getPresentableUrl()));
                }

                if (rootType.equals(BinariesOrderRootType.ID) && file.getName().equals("source")) {
                    if (containsDLangFiles(file)) {
                        result.add(file);
                    }
                    return false;
                }

                return true;
            }
        });
    }
}

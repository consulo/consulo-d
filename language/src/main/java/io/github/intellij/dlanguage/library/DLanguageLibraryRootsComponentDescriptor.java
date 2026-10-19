package io.github.intellij.dlanguage.library;

import consulo.content.OrderRootType;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.DocumentationOrderRootType;
import consulo.content.library.ui.AttachRootButtonDescriptor;
import consulo.content.library.ui.DocumentationUtil;
import consulo.content.library.ui.LibraryEditor;
import consulo.content.library.ui.LibraryRootsComponentDescriptor;
import consulo.content.library.ui.OrderRootTypePresentation;
import consulo.content.library.ui.RootDetector;
import consulo.d.localize.DLocalize;
import consulo.dataContext.DataContext;
import consulo.ide.ui.OrderRootTypeUIFactory;
import consulo.project.localize.ProjectLocalize;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Port of upstream {@code DLangLibrary.kt}.
 */
public class DLanguageLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {

    @Override
    public @Nullable OrderRootTypePresentation getRootTypePresentation(OrderRootType type) {
        if (LibFileRootType.ID.equals(type.getId())) {
            return new OrderRootTypePresentation(DLocalize.libraryLibFileRootTypePresentation().get(), DLanguage.Icons.FILE);
        }
        else {
            // following copied from:
            // com.intellij.openapi.roots.ui.configuration.libraryEditor.DefaultLibraryRootsComponentDescriptor
            // as it's not available across all IDEs
            OrderRootTypeUIFactory factory = OrderRootTypeUIFactory.forOrderType(type);
            // Consulo: a root type without a UI factory falls back to the platform's default presentation
            if (factory == null) {
                return null;
            }
            return new OrderRootTypePresentation(factory.getNodeText().get(), factory.getIcon());
        }
    }

    @Override
    public List<? extends RootDetector> getRootDetectors() {
        return List.of(
            new DlangLibRootDetector(BinariesOrderRootType.ID, DLocalize.sourcesRootDetectorSourcesName()),
            new DlangLibRootDetector(LibFileRootType.ID, DLocalize.sourcesRootDetectorLibName()));
    }

    @Override
    public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
        return List.of(new AttachUrlJavadocDescriptor());
    }

    private static class AttachUrlJavadocDescriptor extends AttachRootButtonDescriptor {
        AttachUrlJavadocDescriptor() {
            super(DocumentationOrderRootType.ID, ProjectLocalize.moduleLibrariesJavadocUrlButton());
        }

        @RequiredUIAccess
        @Override
        public CompletableFuture<VirtualFile[]> selectFiles(@Nullable Component parent,
                                                            @Nullable VirtualFile initialSelection,
                                                            DataContext dataContext,
                                                            LibraryEditor libraryEditor) {
            return DocumentationUtil.showSpecifyJavadocUrlDialogAsync(parent, "")
                .thenApply(vFile -> vFile != null ? new VirtualFile[]{vFile} : VirtualFile.EMPTY_ARRAY);
        }
    }
}

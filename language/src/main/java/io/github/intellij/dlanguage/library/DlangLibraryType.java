package io.github.intellij.dlanguage.library;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.content.library.DummyLibraryProperties;
import consulo.content.library.LibraryType;
import consulo.content.library.LibraryTypeService;
import consulo.content.library.NewLibraryConfiguration;
import consulo.content.library.PersistentLibraryKind;
import consulo.content.library.ui.LibraryEditorComponent;
import consulo.content.library.ui.LibraryPropertiesEditor;
import consulo.content.library.ui.LibraryRootsComponentDescriptor;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.library.ModuleAwareLibraryType;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@ExtensionImpl
public class DlangLibraryType extends ModuleAwareLibraryType<DummyLibraryProperties> {

    public static final PersistentLibraryKind<DummyLibraryProperties> DLANG_LIBRARY =
        new PersistentLibraryKind<DummyLibraryProperties>("DLangLib") {
            @Override
            public DummyLibraryProperties createDefaultProperties() {
                return new DummyLibraryProperties();
            }
        };

    public DlangLibraryType() {
        super(DLANG_LIBRARY);
    }

    public static DlangLibraryType getInstance() {
        return Application.get().getExtensionPoint(LibraryType.class).findExtensionOrFail(DlangLibraryType.class);
    }

    @Override
    public String getCreateActionName() {
        return DLocalize.libraryTypeCreateActionName().get();
    }

    // Consulo has no module types: a D module is a module with the D extension
    @Override
    public boolean isAvailable(final ModuleRootLayer model) {
        return model.getExtension(DModuleExtension.class) != null;
    }

    @RequiredUIAccess
    @Override
    public CompletableFuture<NewLibraryConfiguration> createNewLibrary(@Nullable final Component parentComponent,
                                                                       @Nullable final VirtualFile contextDirectory,
                                                                       final Project project) {

        return LibraryTypeService.getInstance()
            .createLibraryFromFiles(createLibraryRootsComponentDescriptor(), parentComponent, contextDirectory, this, project);
    }

    @Override
    public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
        return new DLanguageLibraryRootsComponentDescriptor();
    }

    @Override
    public @Nullable LibraryPropertiesEditor createPropertiesEditor(final LibraryEditorComponent<DummyLibraryProperties> component) {
        return null;
    }

    @Override
    public Image getIcon() {
        return DLanguage.Icons.FILE;
    }
}

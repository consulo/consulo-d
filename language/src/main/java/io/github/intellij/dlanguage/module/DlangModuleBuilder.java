package io.github.intellij.dlanguage.module;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTypeId;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DMutableModuleExtension;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.ui.image.Image;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.DlangSdkType;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Consulo has no {@code ModuleBuilder}: the new module wizard ({@code consulo.d.dub.newProject}) fills a builder with what
 * IntelliJ's {@code ModuleBuilder} held (name, content root, SDK) and calls {@link #setupRootModel}.
 */
public class DlangModuleBuilder {

    private static final Logger LOG = Logger.getInstance(DlangModuleBuilder.class);

    private final String myBuilderId;
    private final LocalizeValue myPresentableName;
    private final LocalizeValue myDescription;

    @Nullable
    private List<String> sourcePaths;

    @Nullable
    private Sdk myJdk;
    @Nullable
    private String myName;
    @Nullable
    private String myContentEntryPath;

    public DlangModuleBuilder() {
        this("DLangDmdApp", DLocalize.moduleTitle(), DLocalize.moduleDescription());
    }

    public DlangModuleBuilder(@Nullable final Sdk sdk) {
        this();
        this.setModuleJdk(sdk);
    }

    protected DlangModuleBuilder(final String builderId,
                                 final LocalizeValue presentableName,
                                 final LocalizeValue description) {
        myBuilderId = builderId;
        myPresentableName = presentableName;
        myDescription = description;
    }

    public String getBuilderId() {
        return myBuilderId;
    }

    public Image getNodeIcon() {
        return DLanguage.Icons.FILE;
    }

    public LocalizeValue getDescription() {
        return myDescription;
    }

    public LocalizeValue getPresentableName() {
        return myPresentableName;
    }

    public LocalizeValue getGroupName() {
        return DLocalize.moduleGroupName();
    }

    public int getWeight() {
        return 900;
    }

    public @Nullable Sdk getModuleJdk() {
        return myJdk;
    }

    public void setModuleJdk(@Nullable final Sdk jdk) {
        myJdk = jdk;
    }

    public @Nullable String getName() {
        return myName;
    }

    public void setName(@Nullable final String name) {
        myName = name;
    }

    public @Nullable String getContentEntryPath() {
        return myContentEntryPath;
    }

    public void setContentEntryPath(@Nullable final String contentEntryPath) {
        myContentEntryPath = contentEntryPath;
    }

    public void setupRootModel(final ModifiableRootModel rootModel) {
        // Consulo: the SDK belongs to the D module extension (rootModel.setSdk + inheritSdk upstream)
        final DMutableModuleExtension extension = rootModel.getExtensionWithoutCheck(DMutableModuleExtension.class);
        if (extension != null) {
            extension.setEnabled(true);
            if (myJdk != null) {
                extension.getInheritableSdk().set(null, myJdk);
            }
            if (rootModel.findModuleExtensionSdkEntry(extension) == null) {
                rootModel.addModuleExtensionSdkEntry(extension);
            }
        }
        LOG.debug(String.format("Dlang project SDK set as: %s", myJdk));

        final ContentEntry contentEntry = doAddContentEntry(rootModel);
        if (contentEntry != null) {
            final List<String> paths = getSourcePaths();
            for (final String sourcePath : paths != null ? paths : Collections.<String>emptyList()) {
                final VirtualFile sourceRoot = LocalFileSystem.getInstance()
                            .refreshAndFindFileByPath(FileUtil.toSystemIndependentName(sourcePath));

                if (sourceRoot != null) {
                    contentEntry.addFolder(sourceRoot, ProductionContentFolderTypeProvider.getInstance());
                }
            }
        }

        // Consulo: the "Compile with DMD" and "Run D App" run configurations upstream created here are not created,
        // the D run configuration types live in the execution module, which this one cannot see

        LOG.debug("Dlang project root configured");
    }

    /**
     * The content entry of {@link #getContentEntryPath()}: the one the new module wizard already added, else a new one.
     */
    protected @Nullable ContentEntry doAddContentEntry(final ModifiableRootModel rootModel) {
        final String contentEntryPath = getContentEntryPath();
        if (contentEntryPath == null) {
            return null;
        }
        new File(contentEntryPath).mkdirs();
        final VirtualFile moduleContentRoot = LocalFileSystem.getInstance()
            .refreshAndFindFileByPath(FileUtil.toSystemIndependentName(contentEntryPath));
        if (moduleContentRoot == null) {
            return null;
        }
        for (final ContentEntry contentEntry : rootModel.getContentEntries()) {
            if (moduleContentRoot.equals(contentEntry.getFile())) {
                return contentEntry;
            }
        }
        return rootModel.addContentEntry(moduleContentRoot);
    }

    /* By default, sources are located in {WORKING_DIR}/source folder. */
    @Nullable
    public List<String> getSourcePaths() {
        if (sourcePaths == null || sourcePaths.isEmpty()) {
            final String path = getContentEntryPath() + File.separator + "source";

            if(createSourceDirIfNotExists(path)) {
                sourcePaths = Collections.singletonList(path);
            }
        }
        return sourcePaths;
    }

    public boolean isSuitableSdkType(final SdkTypeId sdkType) {
        return sdkType instanceof DlangSdkType;
    }

    private boolean createSourceDirIfNotExists(final String path) {
        final File sourceDir = new File(path);

        if(sourceDir.exists()) {
            return true;
        } else {
            if(FileUtil.createDirectory(sourceDir)) {
                LOG.info("Create source folder: " + path);
                return true;
            } else {
                LOG.warn("Failed to create source folder: " + path);
                return false;
            }
        }
    }
}

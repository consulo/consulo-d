package io.github.intellij.dlanguage;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.FileEditor;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.platform.Platform;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.project.localize.ProjectLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.index.DModuleIndex;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class is used to inform users that DMD is not setup for the project
 * <p>
 * Port of upstream {@code DLanguage.kt}. Consulo has no {@code ProjectSdkSetupValidator}; this is the editor
 * notification that IntelliJ builds from one. Consulo has no project SDK either: the SDK is the one of the D module
 * extension of the file's module, and the fix opens that module's settings.
 */
@ExtensionImpl
public class DLangProjectDmdSetupValidator implements EditorNotificationProvider {

    // basic set of packages in phobos. Due to packages being moved around in phobos we can't check all of them.
    // At some point, if we can support language versions (need to implement com.intellij.openapi.projectRoots.Sdk)
    // then perhaps we can verify phobos packages relevant to the version of D being used.
    private static final Set<String> EXPECTED_MODULES = Collections.unmodifiableSet(new LinkedHashSet<>(List.of(
        //"std.algorithm", // directory
        "std.array",
        "std.ascii",
        "std.base64",
        "std.bigint",
        "std.bitmanip",
        //"std.checkedint", see #770: Don't check "std.checkedint" as before D 2.099 it was "std.experimental.checkedint"
        "std.compiler",
        "std.complex",
        "std.concurrency",
        //"std.container", // directory
        "std.conv",
        "std.csv",
        //"std.datetime", // directory
        "std.demangle",
        //"std.digest", // directory
        "std.encoding",
        "std.exception",
        //"std.experimental", // directory
        "std.file",
        //"std.format", // directory
        "std.functional",
        "std.getopt",
        //"std.int128", // signed 128 bit integer type was added in 2.100.0 (we should check for it in later plugin releases)
        //"std.internal", // directory
        "std.json",
        //"std.math", // directory
        "std.mathspecial",
        "std.meta",
        "std.mmfile",
        "std.numeric",
        "std.outbuffer",
        "std.parallelism",
        "std.path",
        "std.process",
        "std.random", // The random number generators and API provided in this module are not designed to be cryptographically secure
        "std.signals",
        "std.socket",
        "std.stdint",
        "std.stdio",
        "std.string",
        "std.sumtype",
        "std.system",
        "std.traits",
        "std.typecons",
        "std.typetuple",
        //"std.uni", // directory
        "std.uni",
        "std.uri",
        "std.utf",
        "std.uuid",
        "std.variant", // the Algebraic template in this package is outdated and not recommended for use in new code. Instead, use std.sumtype.SumType.
        //"std.windows", // directory
        //"std.xml", // This module is considered out-dated and not up to Phobos' current standards. It will be removed from Phobos in 2.101.0
        "std.zip",
        "std.zlib"
    )));

    private final Project myProject;

    @Inject
    public DLangProjectDmdSetupValidator(Project project) {
        myProject = project;
    }

    @Override
    public String getId() {
        return "d-sdk-setup";
    }

    @RequiredReadAction
    @Override
    public @Nullable EditorNotificationBuilder buildNotification(VirtualFile file,
                                                                 FileEditor fileEditor,
                                                                 Supplier<EditorNotificationBuilder> builderFactory) {
        if (!isApplicableFor(myProject, file)) {
            return null;
        }
        LocalizeValue errorMessage = getErrorMessage(myProject, file);
        if (errorMessage == null) {
            return null;
        }
        Module module = ModuleUtilCore.findModuleForFile(file, myProject);
        EditorNotificationBuilder builder = builderFactory.get();
        builder.withText(errorMessage);
        // This entrypoint is when the user chooses to fix the issue by clicking on the panel over the editor pane
        builder.withAction(ProjectLocalize.moduleSdkSetup(), event -> configureSdkForModule(myProject, module));
        return builder;
    }

    @RequiredReadAction
    private boolean isApplicableFor(Project project, VirtualFile file) {
        if (DlangFileType.INSTANCE != file.getFileType()) {
            return false;
        }
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module == null) {
            return true;
        }
        Sdk projectSdk = ModuleUtilCore.getSdk(module, DModuleExtension.class);
        if (projectSdk == null) {
            return true;
        }
        if (!(projectSdk.getSdkType() instanceof DlangSdkType sdkType)) {
            return true;
        }

        String homePath = projectSdk.getHomePath();
        if (homePath == null || !sdkType.isValidSdkHome(Platform.current(), Path.of(homePath))) {
            return true;
        }

        // Don't check for object.d or phobos unless the index is ready
        if (!DumbService.isDumb(project)) {
            if (cannotFindObjectDotD(project)) {
                return true;
            }
            if (missingAnyPhobosFiles(project)) {
                return true;
            }
            return false;
        }

        return false;
    }

    @RequiredReadAction
    private boolean missingAnyPhobosFiles(Project project) {
        return !missingPhobosModules(project).isEmpty();
    }

    @RequiredReadAction
    private List<String> missingPhobosModules(Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<String> missing = new ArrayList<>();
        for (String it : EXPECTED_MODULES) {
            if (DModuleIndex.getFilesByModuleName(project, it, scope).isEmpty()) {
                missing.add(it);
            }
        }
        return missing;
    }

    @RequiredReadAction
    private boolean cannotFindObjectDotD(Project project) {
        List<DlangPsiFile> files = DModuleIndex.getFilesByModuleName(project, "object", GlobalSearchScope.allScope(project));
        return files.isEmpty();
    }

    /*
     * This function replaces the previously used doFix()
     * It was changed when working on https://github.com/intellij-dlanguage/intellij-dlanguage/issues/679
     */
    @RequiredUIAccess
    private static void configureSdkForModule(Project project, @Nullable Module module) {
        if (!project.isDisposed()) {
            if (module != null && !module.isDisposed()) {
                ShowSettingsUtil.getInstance().showProjectStructureDialog(project, it -> it.select(module, true));
            }
            else {
                ShowSettingsUtil.getInstance().showProjectStructureDialog(project);
            }
        }
    }

    @RequiredReadAction
    private @Nullable LocalizeValue getErrorMessage(Project project, VirtualFile file) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);

        if (module != null && !module.isDisposed()) {
            Sdk sdk = ModuleUtilCore.getSdk(module, DModuleExtension.class);
            if (sdk == null) {
                return DLocalize.dUiModuleSdkNotDefined();
            }
            else if (sdk.getSdkType() instanceof DlangSdkType && sdk.getRootProvider().getFiles(SourcesOrderRootType.ID).length == 0) {
                return DLocalize.dUiCompilerconfigMissingSourcepaths();
            }
            else if (sdk.getSdkType() instanceof DlangSdkType && !DumbService.isDumb(project)) {
                if (cannotFindObjectDotD(project)) {
                    return DLocalize.dUiDmdObjectPathNotSet();
                }
                if (missingAnyPhobosFiles(project)) {
                    String missing = "";
                    List<String> modules = missingPhobosModules(project);
                    for (int i = modules.size() - 1; i >= 0; i--) {
                        missing = missing + " " + modules.get(i);
                    }
                    return LocalizeValue.join(DLocalize.dUiDmdPhobosPathNotSet(), LocalizeValue.of(missing));
                }
                return null;
            }
        }
        return null;
    }
}

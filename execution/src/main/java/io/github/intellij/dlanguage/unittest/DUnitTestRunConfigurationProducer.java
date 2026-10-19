package io.github.intellij.dlanguage.unittest;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@ExtensionImpl
public class DUnitTestRunConfigurationProducer extends RunConfigurationProducer<DUnitTestRunConfiguration> {

    public DUnitTestRunConfigurationProducer() {
        super(DUnitTestRunConfigurationType.getInstance());
    }

    @RequiredReadAction
    @Override
    protected boolean setupConfigurationFromContext(final DUnitTestRunConfiguration configuration,
                                                    final ConfigurationContext context,
                                                    final SimpleReference<PsiElement> sourceElement) {
        final VirtualFile dFile = getRunnableDFileFromContext(context);
        if (dFile != null) {
            configuration.setdFilePath(dFile.getPath());
            configuration.setWorkingDir(Objects.requireNonNull(dFile.getParent()).getPath());
            configuration.setGeneratedName();

            final PsiElement element = sourceElement.get();
            sourceElement.set(element == null ? null : element.getContainingFile());
            return true;
        }
        return false;
    }

    @RequiredReadAction
    private static @Nullable VirtualFile getRunnableDFileFromContext(final ConfigurationContext context) {
        final PsiElement psiLocation = context.getPsiLocation();
        final PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
        final VirtualFile virtualFile = getRealVirtualFile(psiFile);

        if ((psiFile instanceof DlangPsiFile) &&
                virtualFile != null &&
                ProjectRootManager.getInstance(context.getProject()).getFileIndex().isInContent(virtualFile) &&
                !isInDLanguageSdkOrDLanguagePackagesFolder(psiFile.getProject(), virtualFile)) {

            // only run this producer if is test file
            if (DUtil.isDunitTestFile(psiFile)) {
                return virtualFile;
            } else {
                return null;
            }
        }

        return null;
    }

    @RequiredReadAction
    @Override
    public boolean isConfigurationFromContext(final DUnitTestRunConfiguration configuration,
                                              final ConfigurationContext context) {
        final VirtualFile dartFile = getDFileFromContext(context);
        return dartFile != null && dartFile.getPath().equals(configuration.getdFilePath());
    }

    @RequiredReadAction
    private static @Nullable VirtualFile getDFileFromContext(final ConfigurationContext context) {
        final @Nullable PsiElement psiLocation = context.getPsiLocation();
        final PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
        final VirtualFile virtualFile = getRealVirtualFile(psiFile);
        return psiFile instanceof DlangPsiFile && virtualFile != null ? virtualFile : null;
    }

    public static @Nullable VirtualFile getRealVirtualFile(final @Nullable PsiFile psiFile) {
        return psiFile != null ? psiFile.getOriginalFile().getVirtualFile() : null;
    }

    /**
     * Copy of upstream's {@code DlangWritingAccessProvider.isInDLanguageSdkOrDLanguagePackagesFolder}; the writing
     * access provider is not part of this port.
     */
    static boolean isInDLanguageSdkOrDLanguagePackagesFolder(final Project project, final VirtualFile file) {
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

        if (fileIndex.isInLibraryClasses(file)) {
            return true; // file in SDK or in custom package root
        }

        return false;
    }
}

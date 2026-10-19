package io.github.intellij.dub.run;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

/**
 * Port of upstream {@code DlangRunDubConfigurationProducer.kt}. Upstream also skipped files of the D SDK and of DUB
 * packages; those are never in the project content, which is checked already.
 */
@ExtensionImpl
public class DlangRunDubConfigurationProducer extends RunConfigurationProducer<DlangRunDubConfiguration> {
    private static final Logger log = Logger.getInstance(DlangRunDubConfigurationProducer.class);

    public DlangRunDubConfigurationProducer() {
        super(DlangRunDubConfigurationType.getInstance());
    }

    @RequiredReadAction
    private @Nullable VirtualFile getRunnableDFileFromContext(ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
        VirtualFile virtualFile = getRealVirtualFile(psiFile);
        if (psiFile instanceof DlangPsiFile && virtualFile != null &&
            ProjectRootManager.getInstance(context.getProject()).getFileIndex().isInContent(virtualFile)) {

            // don't try to run this producer if is a test file
            if (DUtil.isDunitTestFile(psiFile)) {
                log.debug(psiFile.getName() + " is a dunit test file");
                return null;
            }
            return virtualFile;
        }
        return null;
    }

    @RequiredReadAction
    private @Nullable VirtualFile getDFileFromContext(ConfigurationContext context) {
        PsiElement psiLocation = context.getPsiLocation();
        PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
        VirtualFile virtualFile = getRealVirtualFile(psiFile);
        return psiFile instanceof DlangPsiFile && virtualFile != null ? virtualFile : null;
    }

    private @Nullable VirtualFile getRealVirtualFile(@Nullable PsiFile psiFile) {
        return psiFile == null ? null : psiFile.getOriginalFile().getVirtualFile();
    }

    @RequiredReadAction
    @Override
    protected boolean setupConfigurationFromContext(DlangRunDubConfiguration configuration,
                                                    ConfigurationContext context,
                                                    SimpleReference<PsiElement> sourceElement) {
        VirtualFile dFile = getRunnableDFileFromContext(context);
        if (dFile != null) {
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            configuration.setName("Dub");
            return true;
        }
        return false;
    }

    @RequiredReadAction
    @Override
    public boolean isConfigurationFromContext(DlangRunDubConfiguration configuration, ConfigurationContext context) {
        VirtualFile dFile = getDFileFromContext(context);
        return dFile != null;
    }
}

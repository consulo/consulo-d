package io.github.intellij.dlanguage.inspections;

import consulo.application.Application;
import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;

public class SetupSdkQuickFix extends LocalQuickFixOnPsiElement implements HighPriorityAction {
    public SetupSdkQuickFix(PsiFile file) {
        super(file);
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixSetupsdkquickfixText();
    }

    /**
     * Consulo: a module has no project SDK to inherit, the D SDK is chosen in the module's D extension, so this fix
     * cannot do what upstream's does and is not offered (as consulo-java's {@code SetupJDKFix}).
     */
    @Override
    public boolean isAvailable(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        return false;
    }

    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
//        val projectJdk = ProjectSettingsService.getInstance(project).chooseAndSetSdk() ?: return
        Application.get().runWriteAction(() -> {
            Module module = ModuleUtilCore.findModuleForPsiElement(file);
            if (module != null) {
                // Consulo: no ModuleRootModificationUtil.setSdkInherited(module), see isAvailable
            }
        });
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}

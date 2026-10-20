package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import io.github.intellij.dlanguage.psi.DLanguageGccAsmTemplate;
import io.github.intellij.dlanguage.psi.ext.DLanguageGccAsmTemplateImplMixin;

/**
 * The instruction template of a GCC style asm statement ({@code asm { "movl %1, %0" : "=r" (r) : "r" (x); }}) is
 * assembly in the GNU as dialect of the target.
 */
@ExtensionImpl
public class DGccAsmInjector implements MultiHostInjector {
    @Override
    public Class<? extends PsiElement> getElementClass() {
        return DLanguageGccAsmTemplate.class;
    }

    @RequiredReadAction
    @Override
    public void injectLanguages(MultiHostRegistrar registrar, PsiElement context) {
        if (!(context instanceof DLanguageGccAsmTemplateImplMixin template)) {
            return;
        }
        PsiFile file = template.getContainingFile();
        LanguageVersion version = DAsmDialects.forFile(template.getProject(), file.getOriginalFile().getVirtualFile());
        registrar.startInjecting(version)
            .addPlace(null, null, template, template.getValueRange())
            .doneInjecting();
    }
}

package consulo.d.execution.debugger;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.nativeDev.debugger.NativeDebuggerLanguageSupport;
import consulo.project.Project;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

/**
 * Evaluating D expressions in the native debuggers: gdb understands D ({@code set language d}), lldb falls back to C.
 */
@ExtensionImpl
public class DNativeDebuggerLanguageSupport implements NativeDebuggerLanguageSupport {
    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public String getDebuggerLanguage() {
        return "d";
    }

    @Override
    public PsiFile createExpressionCodeFragment(Project project, String text, @Nullable PsiElement context, boolean isPhysical) {
        return PsiFileFactory.getInstance(project).createFileFromText("fragment.d", DLanguage.INSTANCE, text, isPhysical, false);
    }
}

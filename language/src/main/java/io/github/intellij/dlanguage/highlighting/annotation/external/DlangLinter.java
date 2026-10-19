package io.github.intellij.dlanguage.highlighting.annotation.external;

import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.highlighting.annotation.DProblem;

public interface DlangLinter {

    DProblem[] checkFileSyntax(final PsiFile file);
}

package io.github.intellij.dlanguage.template;

import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.language.editor.surroundWith.Surrounder;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

// a placeholder for later to enable language specific template expansion.
public class ExampleSurrounder implements Surrounder {
    @Override
    public LocalizeValue getTemplateDescription() {
        throw new UnsupportedOperationException("An operation is not implemented: not implemented"); //To change body of created functions use File | Settings | File Templates.
    }

    @Override
    public @Nullable TextRange surroundElements(Project project, Editor editor, PsiElement[] elements) {
        throw new UnsupportedOperationException("An operation is not implemented: not implemented"); //To change body of created functions use File | Settings | File Templates.
    }

    @Override
    public boolean isApplicable(PsiElement[] elements) {
        throw new UnsupportedOperationException("An operation is not implemented: not implemented"); //To change body of created functions use File | Settings | File Templates.
    }
}

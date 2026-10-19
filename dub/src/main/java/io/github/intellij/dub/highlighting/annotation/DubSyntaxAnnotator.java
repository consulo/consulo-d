package io.github.intellij.dub.highlighting.annotation;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.dumb.DumbAware;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.Language;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.ExternalAnnotator;
import consulo.language.psi.PsiFile;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.highlighting.annotation.DProblem;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class DubSyntaxAnnotator extends ExternalAnnotator<PsiFile, DubSyntaxAnnotator.State> implements DumbAware {
    public record State(DProblem[] annotations) {
    }

    @Override
    public PsiFile collectInformation(PsiFile file, Editor editor, boolean hasErrors) {
        return collectInformation(file);
    }

    @Override
    public PsiFile collectInformation(PsiFile file) {
        return file;
    }

    @Override
    public void apply(PsiFile file, @Nullable State annotationResult, AnnotationHolder holder) {
        super.apply(file, annotationResult, holder);
        if (annotationResult == null) {
            return;
        }
        if (annotationResult.annotations().length == 0 || !file.isValid()) {
            return;
        }
        for (DProblem annotation : annotationResult.annotations()) {
            annotation.createAnnotations(file, holder);
        }
    }

    @Override
    public @Nullable State doAnnotate(@Nullable PsiFile collectedInfo) {
        if (collectedInfo == null) {
            return null;
        }

        // Force all files to save to ensure annotations are in sync with the file system.
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        Application application = Application.get();

        if (fileDocumentManager.getUnsavedDocuments().length > 0) {
            application.invokeLater(
                () -> {
                    for (Document document : fileDocumentManager.getUnsavedDocuments()) {
                        fileDocumentManager.saveDocumentAsIs(document);
                    }
                },
                application.getDefaultModalityState()
            );
        }

        return new State(new CompileCheck().checkFileSyntax(collectedInfo));
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

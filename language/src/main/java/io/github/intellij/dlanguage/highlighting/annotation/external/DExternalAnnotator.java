package io.github.intellij.dlanguage.highlighting.annotation.external;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.Language;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.ExternalAnnotator;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.highlighting.annotation.DProblem;
import org.jspecify.annotations.Nullable;


/**
 * Single annotator that calls all external tools used for annotations.
 */
@ExtensionImpl
public class DExternalAnnotator extends ExternalAnnotator<PsiFile, DExternalAnnotator.State> {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger LOG = Logger.getInstance(DExternalAnnotator.class);

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    /**
     * The default implementation here is to not annotate files that have lexer/parser errors.  This is kind
     * of lame since the error may be invalid.
     */
    @Override
    public @Nullable PsiFile collectInformation(final PsiFile file, final Editor editor, final boolean hasErrors) {
        return collectInformation(file);
    }

    @Override
    public PsiFile collectInformation(final PsiFile file) {
        return file;
    }

    /**
     * Collects full information required for annotation. This method is intended for long-running activities
     * and will be called outside a read action; implementations should either avoid accessing indices and PSI or
     * perform needed checks and locks themselves.
     *
     * @param file initial information gathered by {@link ExternalAnnotator#collectInformation}
     * @return annotations to pass to {@link DExternalAnnotator#apply(PsiFile, State, AnnotationHolder)}
     */
    @Override
    public @Nullable State doAnnotate(final PsiFile file) {
        // Force all files to save to ensure annotations are in sync with the file system.
        final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        final Application application = Application.get();

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

        return new State(
            new DScanner().checkFileSyntax(file)
        );
    }

    /**
     * Applies collected annotations (state) to the given AnnotationHolder. This method is called within a read action.
     *
     * @param file             a file to annotate
     * @param state annotations collected in {@link DExternalAnnotator#doAnnotate(PsiFile)}
     * @param holder           a container for receiving annotations
     */
    @Override
    public void apply(final PsiFile file, final State state, final AnnotationHolder holder) {
        createAnnotations(file, state.getdScannerProblems(), holder);
    }

    private void createAnnotations(final PsiFile file,
                                   final DProblem @Nullable [] problems,
                                   final AnnotationHolder holder) {
        if (problems == null || problems.length == 0 || !file.isValid()) return;
        for (final DProblem problem : problems) {
            if(problem != null) {
                problem.createAnnotations(file, holder);
            }
        }
    }

    public static class State {
        private final DProblem[] dScannerProblems;

        public State(final DProblem[] dScannerProblems) {
            this.dScannerProblems = dScannerProblems;
        }

        public DProblem[] getdScannerProblems() {
            return dScannerProblems;
        }
    }
}

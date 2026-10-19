package io.github.intellij.dlanguage.highlighting.exitpoint;

import consulo.application.ReadAction;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.document.util.TextRange;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DHighlightExitPointsHandlerFactoryTest.kt}. Upstream runs IntelliJ's
 * {@code HighlightUsagesHandler.invoke} and reads the editor's highlighters; Consulo keeps that class internal, so the
 * handler the factory creates for the element at the caret computes the usages itself, which is what {@code invoke}
 * highlights. The element is taken from the markup's caret: consulo-it's headless editor cannot move its caret over a
 * document it was opened on (its line cache stays empty until the document changes).
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DHighlightExitPointsHandlerFactoryTest {

    private void doTest(CodeInsightTestFixture fixture, String fileName, String... usages) throws Exception {
        fixture.configureByText(fileName, load("/gold/highlighting/exitpoints/" + fileName));
        Project project = fixture.getProject();
        PsiFile file = fixture.getFile();
        int caretOffset = fixture.getExpectedHighlightingData().getCaretOffset();
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = project.getUIAccess().giveAsync(() -> editorFactory.createEditor(fixture.getDocument(), project)).join();
        try {
            List<String> actual = ReadAction.compute(() -> {
                PsiElement target = file.findElementAt(caretOffset);
                assertNotNull(target, "nothing at the caret");
                HighlightUsagesHandlerBase<PsiElement> handler = new DHighlightExitPointsHandlerFactory().createHighlightUsagesHandler(editor, file, target);
                assertNotNull(handler, "no exit points handler at the caret");
                handler.computeUsages(handler.getTargets());
                String text = file.getText();
                List<String> texts = new ArrayList<>();
                for (TextRange range : handler.getReadUsages()) {
                    texts.add(range.substring(text));
                }
                texts.sort(null);
                return texts;
            });
            List<String> expected = new ArrayList<>(Arrays.asList(usages));
            expected.sort(null);
            assertEquals(expected, actual);
        }
        finally {
            project.getUIAccess().giveAsync(() -> {
                editorFactory.releaseEditor(editor);
                return null;
            }).join();
        }
    }

    @Test
    public void testAllReturns(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "all_returns.d", "return 1;", "return 2;");
    }

    @Test
    public void testReturnsAndThrows(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "returns_and_throws.d", "return 1;", "return 2;", "throw new Error(\"Oh no!\")");
    }

    @Test
    public void testFunctionLiterals(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "function_literals.d", "return 6;", "return 7;", "throw new Error(\"Unknown\")");
    }

    private String load(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

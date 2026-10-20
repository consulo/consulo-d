package io.github.intellij.dlanguage.features;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.application.internal.TransferredWriteActionService;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.codeEditor.action.TypedAction;
import consulo.dataContext.DataContext;
import consulo.document.Document;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.it.daemon.ExpectedHighlightingData;
import consulo.language.psi.PsiDocumentManager;
import consulo.project.Project;
import consulo.undoRedo.CommandProcessor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of upstream {@code DQuoteHandlerTest.kt}. IntelliJ's {@code myFixture.type(c)} is reproduced with an editor on
 * the configured document (opened on it empty, as consulo-it's headless editor needs to move its caret) and the
 * application's typing handler run in a write command; {@code checkResult} compares the text and the caret with the
 * text after. Upstream's {@code PsiTestUtil.checkPsiStructureWithCommit} has no Consulo counterpart.
 */
@Disabled("consulo-it cannot type: TypedHandler, the typing handler that applies QuoteHandlers, lives in ide-impl, which"
    + " consulo-it does not bind, so typing only inserts the character")
@ExtendWith(HeadlessProjectExtension.class)
public class DQuoteHandlerTest {

    @Test
    @DisplayName("test complete char quote")
    public void testCompleteCharQuote(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                <caret>
            }
            """, """
            void main() {
                '<caret>'
            }
            """, '\'');
    }

    @Test
    @DisplayName("test complete char quote empty file")
    public void testCompleteCharQuoteEmptyFile(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, "<caret>", "'<caret>'", '\'');
    }

    @Test
    @DisplayName("test complete string double quote")
    public void testCompleteStringDoubleQuote(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                <caret>
            }
            """, """
            void main() {
                "<caret>"
            }
            """, '"');
    }

    @Test
    @DisplayName("test complete alternate wysiwyg quote")
    public void testCompleteAlternateWysiwygQuote(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                <caret>
            }
            """, """
            void main() {
                `<caret>`
            }
            """, '`');
    }

    @Test
    @DisplayName("test complete alternate wysiwyg quote 2")
    public void testCompleteAlternateWysiwygQuote2(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                `<caret>`
            }
            """, """
            void main() {
                `"<caret>`
            }
            """, '"');
    }

    @Test
    @DisplayName("test complete wysiwyg quote")
    public void testCompleteWysiwygQuote(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                r<caret>
            }
            """, """
            void main() {
                r"<caret>"
            }
            """, '"');
    }

    @Test
    @DisplayName("test complete string double quote with string after")
    public void testCompleteStringDoubleQuoteWithStringAfter(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                string new = <caret>
                string existing = "foo";
            }
            """, """
            void main() {
                string new = "<caret>"
                string existing = "foo";
            }
            """, '"');
    }

    @Test
    @DisplayName("test complete string double quote with string after with escaped double quote")
    public void testCompleteStringDoubleQuoteWithStringAfterWithEscapedDoubleQuote(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                string new = <caret>
                string existing = "foo\\"bar";
            }
            """, """
            void main() {
                string new = "<caret>"
                string existing = "foo\\"bar";
            }
            """, '"');
    }

    @Test
    @DisplayName("test complete alternate wysiwig quote with string after")
    public void testCompleteAlternateWysiwigQuoteWithStringAfter(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                string new = <caret>
                string existing = "foo";
            }
            """, """
            void main() {
                string new = `<caret>`
                string existing = "foo";
            }
            """, '`');
    }

    @Test
    @DisplayName("test don't complete escape double quote in string")
    public void testDontCompleteEscapeDoubleQuoteInString(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                "\\<caret>"
            }
            """, """
            void main() {
                "\\"<caret>"
            }
            """, '"');
    }

    @Test
    @DisplayName("test don't complete escape double quote in alternate wysiwyg string")
    public void testDontCompleteEscapeDoubleQuoteInAlternateWysiwygString(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                `\\<caret>`
            }
            """, """
            void main() {
                `\\`<caret>`
            }
            """, '`');
    }

    private void doTestByText(CodeInsightTestFixture fixture, String before, String after, char c) throws Exception {
        fixture.configureByText("main.d", before.strip());
        int caret = type(fixture, c);
        checkResult(fixture, after.strip(), caret);
    }

    /**
     * Types {@code c} at the caret of the configured markup and returns where the caret ends up.
     */
    private static int type(CodeInsightTestFixture fixture, char c) {
        Project project = fixture.getProject();
        Document document = fixture.getDocument();
        String text = ReadAction.compute(document::getText);
        int caret = fixture.getExpectedHighlightingData().getCaretOffset();
        TransferredWriteActionService writeService = Application.get().getInstance(TransferredWriteActionService.class);

        WriteAction.run(() -> writeService.runOnEdtWithTransferredWriteActionAndWait(() -> document.setText("")));
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = project.getUIAccess().giveAsync(() -> editorFactory.createEditor(document, project)).join();
        try {
            WriteAction.run(() -> writeService.runOnEdtWithTransferredWriteActionAndWait(
                () -> CommandProcessor.getInstance().newCommand()
                    .project(project)
                    .document(document)
                    .run(() -> {
                        document.setText(text);
                        PsiDocumentManager.getInstance(project).commitDocument(document);
                        editor.getCaretModel().moveToOffset(caret);
                        DataContext context = DataContext.builder()
                            .add(Project.KEY, project)
                            .add(Editor.KEY, editor)
                            .build();
                        TypedAction.getInstance().getHandler().execute(editor, c, context);
                        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
                    })
            ));
            return project.getUIAccess().giveAsync(() -> editor.getCaretModel().getOffset()).join();
        }
        finally {
            project.getUIAccess().giveAsync(() -> {
                editorFactory.releaseEditor(editor);
                return null;
            }).join();
        }
    }

    private static void checkResult(CodeInsightTestFixture fixture, String after, int caret) {
        ExpectedHighlightingData expected = ExpectedHighlightingData.parse(after);
        assertEquals(expected.getText(), ReadAction.compute(() -> fixture.getDocument().getText()));
        assertEquals(expected.getCaretOffset(), caret);
    }
}

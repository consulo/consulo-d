package io.github.intellij.dlanguage.formatting;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.application.internal.TransferredWriteActionService;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.codeEditor.action.EditorActionManager;
import consulo.codeEditor.action.TypedAction;
import consulo.dataContext.DataContext;
import consulo.document.Document;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.it.daemon.ExpectedHighlightingData;
import consulo.language.psi.PsiDocumentManager;
import consulo.project.Project;
import consulo.ui.ex.action.IdeActions;
import consulo.undoRedo.CommandProcessor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of upstream {@code DAutoIndentTest.kt}. IntelliJ's {@code myFixture.type(c)} is reproduced with an editor on the
 * configured document (opened on it empty, as consulo-it's headless editor needs to move its caret) and, in a write
 * command, the editor's Enter action for {@code '\n'} or the application's typing handler for any other character;
 * {@code checkResult} compares the text and the caret with the text after. Upstream's
 * {@code PsiTestUtil.checkPsiStructureWithCommit} has no Consulo counterpart. The expected texts are what upstream's
 * {@code trimIndent()} makes of its raw strings.
 */
@Disabled("consulo-it cannot press Enter: the Enter handler (EnterHandler, with the EditorActionManager that holds it) lives"
    + " in ide-impl, which consulo-it does not bind, and the indent comes from the formatter, which consulo-it cannot run"
    + " (see DFormatterTest)")
@ExtendWith(HeadlessProjectExtension.class)
public class DAutoIndentTest {

    private String sanitizeText(String text) {
        return trimIndent(text.replace("/*caret*/", "<caret>"));
    }

    private void doTestByText(CodeInsightTestFixture fixture, String before, String after) throws Exception {
        doTestByText(fixture, before, after, '\n');
    }

    private void doTestByText(CodeInsightTestFixture fixture, String before, String after, char c) throws Exception {
        fixture.configureByText("main.d", sanitizeText(before));
        int caret = type(fixture, c);
        checkResult(fixture, sanitizeText(after), caret);
    }

    @Test
    public void testStruct(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            struct A {/*caret*/}
            """, """
            struct A {
                /*caret*/
            }
            """
        );
    }

    @Test
    public void testClass(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            class A {/*caret*/}
            """, """
            class A {
                /*caret*/
            }
            """
        );
    }

    // https://github.com/intellij-dlanguage/intellij-dlanguage/issues/776
    @Test
    public void testEnum(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            enum A {/*caret*/}
            """, """
            enum A {
                /*caret*/
            }
            """
        );
    }

    @Test
    public void testFunction(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {/*caret*/}
            """, """
            void main() {
                /*caret*/
            }
            """
        );
    }

    @Test
    public void testIf(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                if (true) {/*caret*/}
            }
            """, """
            void main() {
                if (true) {
                    /*caret*/
                }
            }
            """
        );
    }

    @Test
    public void testWhile(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                while (true) {/*caret*/}
            }
            """, """
            void main() {
                while (true) {
                    /*caret*/
                }
            }
            """
        );
    }

    @Test
    public void testFor(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                for (int i = 0; i < 10; i++) {/*caret*/}
            }
            """, """
            void main() {
                for (int i = 0; i < 10; i++) {
                    /*caret*/
                }
            }
            """
        );
    }

    @Test
    public void testSwitch(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            void main() {
                int i;
                switch (i) {/*caret*/}
            }
            """, """
            void main() {
                int i;
                switch (i) {
                    /*caret*/
                }
            }
            """
        );
    }

    // TODO fix them
    // /*@Test fun testSwitchCase() = doTestByText("""
    //    void main() {
    //        int i;
    //        switch (i) {
    //            case 1:/*caret*/
    //        }
    //    }
    //""".trimIndent(), """
    //    void main() {
    //        int i;
    //        switch (i) {
    //            case 1:
    //                /*caret*/
    //        }
    //    }
    //""".trimIndent()
    //)
    //
    //@Test fun testSwitchRangeCase() = doTestByText("""
    //    void main() {
    //        int i;
    //        switch (i) {
    //            case 1: .. case 5:/*caret*/
    //        }
    //    }
    //""".trimIndent(), """
    //    void main() {
    //        int i;
    //        switch (i) {
    //            case 1: .. case 5:
    //                /*caret*/
    //        }
    //    }
    //""".trimIndent()
    //)
    //
    //@Test fun testSwitchDefault() = doTestByText("""
    //    void main() {
    //        int i;
    //        switch (i) {
    //            default:/*caret*/
    //        }
    //    }
    //""".trimIndent(), """
    //    void main() {
    //        int i;
    //        switch (i) {
    //            default:
    //                /*caret*/
    //        }
    //    }
    //""".trimIndent()
    //)*/

    // https://github.com/intellij-dlanguage/intellij-dlanguage/issues/780
    @Test
    public void testFunctionCallChaining(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture,
            "void main() {\n"
                + "    Person person = new Person();\n"
                + "    person\n"
                + "     .setName()/*caret*/\n"
                + "}",
            "void main() {\n"
                + "    Person person = new Person();\n"
                + "    person\n"
                + "     .setName()\n"
                + "        /*caret*/\n"
                + "}"
        );
    }

    @Test
    public void testVersion(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            version (A) {/*caret*/}
            """, """
            version (A) {
                /*caret*/
            }
            """
        );
    }

    @Test
    public void testUnittest(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            unittest {/*caret*/}
            """, """
            unittest {
                /*caret*/
            }
            """
        );
    }

    @Test
    public void testUnittestInVersion(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            version (A) {
                unittest {/*caret*/}
            }
            """, """
            version (A) {
                unittest {
                    /*caret*/
                }
            }
            """
        );
    }

    @Test
    public void testSingleLineDoc(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            /// this is a line/*caret*/
            void main() {}
            """, """
            /// this is a line
            /*caret*/
            void main() {}

            """);
    }

    @Test
    public void testMultiLineDoc(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            /**
            this is a line
            *//*caret*/
            void main() {}
            """, """
            /**
            this is a line
            */
            /*caret*/
            void main() {}

            """);
    }

    @Test
    public void testNestedLineDoc(CodeInsightTestFixture fixture) throws Exception {
        doTestByText(fixture, """
            /++
            this is a line
            +//*caret*/
            void main() {}
            """, """
            /++
            this is a line
            +/
            /*caret*/
            void main() {}

            """);
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
                        if (c == '\n') {
                            EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ENTER)
                                .execute(editor, editor.getCaretModel().getCurrentCaret(), context);
                        }
                        else {
                            TypedAction.getInstance().getHandler().execute(editor, c, context);
                        }
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

    /**
     * Kotlin's {@code String.trimIndent()}: drops the first and the last line when blank and the indent all non-blank
     * lines share.
     */
    private static String trimIndent(String text) {
        List<String> lines = new ArrayList<>(text.lines().toList());
        if (text.endsWith("\n")) {
            lines.add("");
        }
        if (!lines.isEmpty() && lines.get(0).isBlank()) {
            lines.remove(0);
        }
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        int minIndent = Integer.MAX_VALUE;
        for (String line : lines) {
            if (!line.isBlank()) {
                int indent = 0;
                while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                    indent++;
                }
                minIndent = Math.min(minIndent, indent);
            }
        }
        int drop = minIndent == Integer.MAX_VALUE ? 0 : minIndent;
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(line.isBlank() ? "" : line.substring(Math.min(drop, line.length())));
        }
        return String.join("\n", result);
    }
}

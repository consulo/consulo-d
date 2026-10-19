package io.github.intellij.dlanguage.template;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.document.Document;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.template.TemplateManager;
import consulo.language.psi.PsiDocumentManager;
import consulo.project.Project;
import consulo.undoRedo.CommandProcessor;
import consulo.util.lang.StringUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of upstream {@code DlangLiveTemplatesTest.kt} and its base {@code DlangTestBase.kt}. Upstream performs the
 * "expand live template by tab" editor action; here the template manager is called the way that action's handler
 * calls it, in a command and a write action, on a headless editor.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DlangLiveTemplatesTest {
    @Test
    @Disabled("consulo-it binds no ProjectUndoManager (Consulo's binding lives in ide-impl), which starting a template needs")
    public void testFDevelopmentTemplate(CodeInsightTestFixture fixture) throws Exception {
        expandSnippet(fixture, """
            struct S {
                f/*caret*/
            }
            """, """
            struct S {
                int foo;
            }
            """);
    }

    @Test
    @Disabled("consulo-it binds no ProjectUndoManager (Consulo's binding lives in ide-impl), which starting a template needs")
    public void testIfThenElseExpansion(CodeInsightTestFixture fixture) throws Exception {
        expandSnippet(fixture, """
            if/*caret*/
            """, """
            if(true) {
               /* statement(s) will execute if the boolean expression is true */
            }
            """);
    }

    private void expandSnippet(CodeInsightTestFixture fixture, String before, String after) throws Exception {
        checkByText(fixture, before.trim(), after.trim());
    }

    private static String replaceCaretMarker(String text) {
        return text.replace("/*caret*/", "<caret>");
    }

    private void checkByText(CodeInsightTestFixture fixture, String before, String after) throws Exception {
        String marked = replaceCaretMarker(before);
        int caretOffset = marked.indexOf("<caret>");
        String text = marked.replace("<caret>", "");

        // consulo-it's headless editor builds its line cache from document changes only, so the file starts empty
        // and gets its text once the editor is open
        fixture.configureByText("main.d", "");
        Project project = fixture.getProject();
        Document document = fixture.getDocument();
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = project.getUIAccess().giveAsync(() -> editorFactory.createEditor(document, project)).join();
        try {
            project.getUIAccess().giveAsync(() -> {
                CommandProcessor.getInstance().newCommand()
                    .project(project)
                    .inWriteAction()
                    .run(() -> document.setText(text));
                PsiDocumentManager.getInstance(project).commitDocument(document);
                editor.getCaretModel().moveToOffset(caretOffset);

                CommandProcessor.getInstance().newCommand()
                    .project(project)
                    .inWriteAction()
                    .run(() -> TemplateManager.getInstance(project).startTemplate(editor, '\t'));
                return null;
            }).join();

            String actual = project.getUIAccess().giveAsync(() -> document.getText()).join();
            assertEquals(StringUtil.convertLineSeparators(replaceCaretMarker(after)), actual);
        }
        finally {
            project.getUIAccess().giveAsync(() -> {
                editorFactory.releaseEditor(editor);
                return null;
            }).join();
        }
    }
}

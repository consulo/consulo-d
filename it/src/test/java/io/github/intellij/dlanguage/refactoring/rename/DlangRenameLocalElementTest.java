package io.github.intellij.dlanguage.refactoring.rename;

import consulo.application.ReadAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.it.daemon.ExpectedHighlightingData;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DlangRenameLocalElementTest.kt}. IntelliJ's {@code renameElementAtCaret} renames the element
 * the reference at the caret resolves to with a {@link RenameProcessor}; {@code checkResult} compares the document with
 * the text after, its caret markup removed.
 */
@Disabled("consulo-it cannot rename: RenameProcessor.run() on its UI thread never returns, as"
    + " BaseRefactoringProcessor.doRun commits the documents under a modal progress (ProgressRunner.execFromEDT) that the"
    + " headless platform never completes; it also binds no PsiSearchHelper, which the usage search needs")
@ExtendWith(HeadlessProjectExtension.class)
public class DlangRenameLocalElementTest {
    @Test
    @DisplayName("test rename class and usages including constructor call")
    public void testRenameClassAndUsagesIncludingConstructorCall(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("main.d", """
            class A {
                this() {}
            }

            void foo() {
                <caret>A a = new A();
            }""");
        renameElementAtCaret(fixture, "B");
        checkResult(fixture, """
            class B {
                this() {}
            }

            void foo() {
                <caret>B a = new B();
            }""");
    }

    private static void renameElementAtCaret(CodeInsightTestFixture fixture, String newName) {
        Project project = fixture.getProject();
        PsiElement element = ReadAction.compute(() -> fixture.getReferenceAtCaretPositionWithAssertion().resolve());
        assertNotNull(element, "the reference at the caret does not resolve");
        RenameProcessor processor = ReadAction.compute(() -> new RenameProcessor(project, element, newName, false, false));
        project.getUIAccess().giveAsync(() -> {
            processor.run();
            return null;
        }).join();
    }

    private static void checkResult(CodeInsightTestFixture fixture, String after) {
        assertEquals(ExpectedHighlightingData.parse(after).getText(), ReadAction.compute(() -> fixture.getDocument().getText()));
    }
}

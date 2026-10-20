package io.github.intellij.dlanguage.refactoring.rename;

import consulo.application.ReadAction;
import consulo.document.FileDocumentManager;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Port of upstream {@code DlangRenameFileActionTest.kt}. IntelliJ's {@code renameElement} runs a {@link RenameProcessor};
 * the files live in the source root of the headless project instead of the light fixture's temp directory.
 */
@Disabled("consulo-it cannot rename: RenameProcessor.run() on its UI thread never returns, as"
    + " BaseRefactoringProcessor.doRun commits the documents under a modal progress (ProgressRunner.execFromEDT) that the"
    + " headless platform never completes; it also binds no PsiSearchHelper, which the usage search needs")
@ExtendWith(HeadlessProjectExtension.class)
public class DlangRenameFileActionTest {

    @Test
    public void testFooToTest(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/main.d", "import std.bar.foo;\nvoid main() {testing();}");
        PsiFile fooFile = fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/std/bar/foo.d", "module std.bar.foo;\nvoid testing() {}");
        renameElement(fixture.getProject(), fooFile, "test.d", false, true);
        fixture.getProject().getUIAccess().giveAsync(() -> {
            FileDocumentManager.getInstance().saveAllDocuments();
            return null;
        }).join();
        String mainAfter = StringUtil.convertLineSeparators(new String(contentsOf(fixture, "main.d"), StandardCharsets.UTF_8));
        String testAfter = StringUtil.convertLineSeparators(new String(contentsOf(fixture, "std/bar/test.d"), StandardCharsets.UTF_8));
        assertEquals("import std.bar.test;\nvoid main() {testing();}", mainAfter);
        assertEquals("module std.bar.test;\nvoid testing() {}", testAfter);
        assertNull(findFileInSourceRoot(fixture, "std/bar/foo.d"));
    }

    private static void renameElement(Project project, PsiFile element, String newName, boolean searchInComments, boolean searchTextOccurrences) {
        RenameProcessor processor = ReadAction.compute(() -> new RenameProcessor(project, element, newName, searchInComments, searchTextOccurrences));
        project.getUIAccess().giveAsync(() -> {
            processor.run();
            return null;
        }).join();
    }

    private static byte[] contentsOf(CodeInsightTestFixture fixture, String path) throws Exception {
        VirtualFile file = findFileInSourceRoot(fixture, path);
        assertNotNull(file, path);
        return file.contentsToByteArray();
    }

    private static @Nullable VirtualFile findFileInSourceRoot(CodeInsightTestFixture fixture, String path) {
        return ReadAction.compute(() -> fixture.getProjectRoot().findFileByRelativePath(CodeInsightTestFixture.SOURCE_ROOT + "/" + path));
    }
}

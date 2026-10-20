package io.github.intellij.dlanguage.annotator;

import consulo.application.ReadAction;
import consulo.document.util.TextRange;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import io.github.intellij.dlanguage.highlighting.annotation.external.LinterHelper;
import io.github.intellij.dlanguage.psi.DlangPsiFileImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code LinterHelperTest.kt}. Upstream's {@code LightDlangTestCase.virtualDlangPsiFile} writes the file
 * into the module's source root and wraps it in a {@link DlangPsiFileImpl}; here the file is written into the source
 * root of the headless project.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class LinterHelperTest {

    @Test
    @DisplayName("test Valid Range Should Highlight Semicolon")
    public void testValidRangeShouldHighlightSemicolon(CodeInsightTestFixture fixture) throws Exception {
        DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "myapp.d", "module ;");

        TextRange range = ReadAction.compute(() -> LinterHelper.calculateTextRange(dlangFile, 1, 8));
        assertNotNull(range);
        assertEquals(1, range.getLength());
        assertEquals(7, range.getStartOffset());
    }

    @Test
    @DisplayName("test Valid Range Highlight Out Of File")
    public void testValidRangeHighlightOutOfFile(CodeInsightTestFixture fixture) throws Exception {
        DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "myapp.d", "module ;");

        TextRange range = ReadAction.compute(() -> LinterHelper.calculateTextRange(dlangFile, 2, 12));
        assertNotNull(range);
        assertEquals(0, range.getLength());
        assertEquals(8, range.getStartOffset());
    }

    private static DlangPsiFileImpl virtualDlangPsiFile(CodeInsightTestFixture fixture, String filename, String content) throws Exception {
        return (DlangPsiFileImpl) fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/" + filename, content);
    }
}

package io.github.intellij.dlanguage.highlighting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class DlangHighlightingLexerTest extends DHighlightingLexerTestBase {
    public DlangHighlightingLexerTest() {
        super("highlighting");
    }

    // Ensure that the parser doesn’t crash if a comment is unclosed
    @Test
    public void testcomment_unclosed() {
        doTest("/* unclosed comment", "DlangTokenType.BLOCK_COMMENT ('/* unclosed comment')", createLexer());
    }

    // strings
    @Test
    public void testchar(TestInfo testInfo) {
        doTest(testInfo);
    }

    @Test
    public void teststrings_escapes(TestInfo testInfo) {
        doTest(testInfo);
    }
}

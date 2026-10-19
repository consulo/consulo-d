package io.github.intellij.dlanguage.sdlang.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class SDLangLexerTest extends SDLangLexerTestBase {

    @Test
    public void testSimple(TestInfo testInfo) {
        doTest(testInfo);
    }

    @Test
    public void testStrings(TestInfo testInfo) {
        doTest(testInfo);
    }

    @Test
    public void testDate(TestInfo testInfo) {
        doTest(testInfo);
    }

    @Test
    public void testNumber(TestInfo testInfo) {
        doTest(testInfo);
    }
}

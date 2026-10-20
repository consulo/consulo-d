package io.github.intellij.dlanguage.highlighting;

import consulo.language.lexer.Lexer;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.lexer.DHighlightingLexer;
import io.github.intellij.dlanguage.lexer.DlangLexerTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code DHighlightingLexerTestBase.kt}. Upstream extends IntelliJ's {@code LexerTestCase}, which
 * Consulo does not have; its token printing comes from {@link DlangLexerTestBase#printTokens(CharSequence, int, Lexer)}.
 */
public abstract class DHighlightingLexerTestBase {
    private final String mySrcPath = getDirPath() + "/" + "highlighting";
    private final String myExpectPath;

    DHighlightingLexerTestBase(String expectPath) {
        myExpectPath = getDirPath() + "/" + expectPath;
    }

    protected void doTest(TestInfo testInfo) {
        doTest(testInfo, true);
    }

    protected void doTest(TestInfo testInfo, boolean checkResult) {
        String fileName = getTestName(testInfo) + ".d";
        String text = "";
        try {
            text = loadFile(fileName);
        }
        catch (IOException e) {
            Assertions.fail("can't load file " + fileName + ": " + e.getMessage());
        }
        String result = printTokens(text, 0);
        if (checkResult) {
            try {
                doCheckResult(
                    myExpectPath + "/" + "expected",
                    getTestName(testInfo) + ".txt", result
                );
            }
            catch (IOException e) {
                Assertions.fail("Unexpected IO Exception: " + e.getMessage());
            }
        }
    }

    /**
     * {@code LexerTestCase.doTest(String, String, Lexer)}: compares the tokens of {@code text} with {@code expected}.
     */
    protected void doTest(String text, String expected, Lexer lexer) {
        String result = DlangLexerTestBase.printTokens(text, 0, lexer);
        Assertions.assertEquals(StringUtil.convertLineSeparators(expected.trim()), StringUtil.convertLineSeparators(result.trim()));
    }

    protected String printTokens(CharSequence text, int start) {
        return DlangLexerTestBase.printTokens(text, start, createLexer());
    }

    protected Lexer createLexer() {
        return new DHighlightingLexer();
    }

    protected String getDirPath() {
        return "gold";
    }

    /**
     * Loads the test data file from the right place.
     */
    protected String loadFile(String name) throws IOException {
        return doLoadFile(mySrcPath, name);
    }

    private String doLoadFile(String myFullDataPath, String name) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(myFullDataPath + "/" + name)) {
            if (stream == null) {
                throw new IOException("No test data: " + myFullDataPath + "/" + name);
            }
            String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
            text = StringUtil.convertLineSeparators(text);
            return text;
        }
    }

    /**
     * Check the result against a plain text file.
     */
    public void doCheckResult(String fullPath, String targetDataName, String actualText) throws IOException {
        String text = actualText.trim();
        String expectedText;
        try {
            expectedText = doLoadFile(fullPath, targetDataName);
        }
        catch (IOException e) {
            Assertions.fail("No output text found: " + fullPath + "/" + targetDataName + "\n" + text);
            return;
        }
        Assertions.assertEquals(expectedText, text, targetDataName);
    }

    /**
     * IntelliJ's {@code getTestName(false)}: the method name without {@code test}, case kept.
     */
    protected static String getTestName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        return name.startsWith("test") ? name.substring("test".length()) : name;
    }
}

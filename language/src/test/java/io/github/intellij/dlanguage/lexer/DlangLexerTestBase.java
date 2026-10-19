package io.github.intellij.dlanguage.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.Lexer;
import consulo.util.lang.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code DlangLexerTestBase.kt}. Upstream extends IntelliJ's {@code LexerTestCase}, which Consulo
 * does not have; the two pieces of it upstream relies on ({@code printTokens} and {@code doTest(text, expected)}) are
 * reproduced here with IntelliJ's output format.
 */
public abstract class DlangLexerTestBase {
    private static final String DIR_PATH = "gold";

    private final String mySrcPath = DIR_PATH + "/" + "lexer";
    private final String myExpectPath;

    protected DlangLexerTestBase(String expectPath) {
        myExpectPath = DIR_PATH + "/" + expectPath;
    }

    protected Lexer createLexer() {
        return new DlangLexer();
    }

    protected void doTest(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        String fileName = testName + ".d";
        String text;
        try {
            text = loadFile(fileName);
        }
        catch (Exception e) {
            Assertions.fail("Something went wrong using file: " + fileName + ": " + e.getMessage());
            return;
        }
        doCheckResult(myExpectPath + "/" + "expected", testName + ".txt", printTokens(text, 0));
    }

    /**
     * {@code LexerTestCase.doTest(String, String)}: compares the tokens of {@code text} with {@code expected}.
     */
    protected void doTest(String text, String expected) {
        String result = printTokens(text, 0);
        assertSameLines(expected, result);
    }

    protected String printTokens(CharSequence text, int start) {
        return printTokens(text, start, createLexer());
    }

    /**
     * {@code LexerTestCase.printTokens}: one {@code TYPE ('text')} line per token.
     */
    public static String printTokens(CharSequence text, int start, Lexer lexer) {
        lexer.start(text, start, text.length());
        StringBuilder result = new StringBuilder();
        while (true) {
            IElementType tokenType = lexer.getTokenType();
            if (tokenType == null) {
                break;
            }
            String tokenText = getTokenText(lexer);
            String tokenTypeName = tokenType.toString();
            result.append(tokenTypeName).append(" ('").append(tokenText).append("')\n");
            lexer.advance();
        }
        return result.toString();
    }

    public static String getTokenText(Lexer lexer) {
        String text = lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
        return StringUtil.replace(text, "\n", "\\n");
    }

    // Loads the test data file from the right place.
    private String loadFile(String name) throws IOException {
        return doLoadFile(mySrcPath, name);
    }

    private String doLoadFile(String fullDataPath, String name) throws IOException {
        String path = String.format("%s/%s", fullDataPath, name);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("No test data: " + path);
            }
            return StringUtil.convertLineSeparators(new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim());
        }
    }

    // Check the result against a plain text file.
    private void doCheckResult(String fullPath, String targetDataName, String text) {
        String theText = text.trim();
        String expectedText;
        try {
            expectedText = doLoadFile(fullPath, targetDataName);
        }
        catch (IOException e) {
            Assertions.fail("No output text found: " + fullPath + "/" + targetDataName + "\n" + theText);
            return;
        }
        Assertions.assertEquals(expectedText, theText, targetDataName);
    }

    private static void assertSameLines(String expected, String actual) {
        Assertions.assertEquals(StringUtil.convertLineSeparators(expected.trim()), StringUtil.convertLineSeparators(actual.trim()));
    }

    /**
     * IntelliJ's {@code getTestName(false)}: the method name without {@code test}, case kept.
     */
    protected static String getTestName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        return name.startsWith("test") ? name.substring("test".length()) : name;
    }
}

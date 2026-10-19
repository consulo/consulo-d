package io.github.intellij.dlanguage.sdlang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.Lexer;
import consulo.util.lang.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code SDLangLexerTestBase.kt}. Upstream extends IntelliJ's {@code LexerTestCase}, which Consulo
 * does not have; the part of it upstream relies on ({@code doFileTest}) is reproduced here with IntelliJ's output
 * format.
 */
public abstract class SDLangLexerTestBase {

    protected void doTest(TestInfo testInfo) {
        doFileTest(testInfo, "sdl");
    }

    protected Lexer createLexer() {
        return new SDLangLexer();
    }

    protected String getDirPath() {
        return "gold/lexer";
    }

    protected String getPathToTestDataFile(TestInfo testInfo, String extension) {
        String name = getTestName(testInfo) + extension;
        return String.format("%s/%s", getDirPath(), name);
    }

    /**
     * {@code LexerTestCase.doFileTest}: lexes {@code <name>.<fileExt>} and compares the tokens with {@code <name>.txt}.
     */
    protected void doFileTest(TestInfo testInfo, String fileExt) {
        String text = StringUtil.convertLineSeparators(loadTestDataFile(getPathToTestDataFile(testInfo, "." + fileExt)).trim());
        String result = printTokens(text, 0, createLexer());
        String expected = StringUtil.convertLineSeparators(loadTestDataFile(getPathToTestDataFile(testInfo, ".txt")));
        Assertions.assertEquals(expected.trim(), result.trim());
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
            String tokenText = StringUtil.replace(
                lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString(), "\n", "\\n");
            result.append(tokenType).append(" ('").append(tokenText).append("')\n");
            lexer.advance();
        }
        return result.toString();
    }

    private String loadTestDataFile(String path) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return Assertions.fail("can't load file " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            return Assertions.fail("can't load file " + path + ": " + e.getMessage());
        }
    }

    /**
     * IntelliJ's {@code getTestName(true)}: the method name without {@code test}, first letter lowercased.
     */
    private static String getTestName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        String trimmed = name.startsWith("test") ? name.substring("test".length()) : name;
        return StringUtil.decapitalize(trimmed);
    }
}

package io.github.intellij.dlanguage.lexer;

import consulo.language.lexer.Lexer;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.features.documentation.DDocLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code DDocLexerTest.kt}. Upstream extends IntelliJ's {@code LexerTestCase} and uses its
 * {@code doFileTest("d")}: {@code <name>.d} is lexed and compared with {@code <name>.txt} next to it.
 */
public class DDocLexerTest {

    private Lexer createLexer() {
        return new DDocLexer();
    }

    private String getDirPath() {
        return "gold" + "/" + "ddoc" + "/" + "lexer";
    }

    private void doTest(TestInfo testInfo) throws IOException {
        String name = getTestName(testInfo);
        String text = loadFile(name + ".d");
        String result = DlangLexerTestBase.printTokens(text, 0, createLexer());
        Assertions.assertEquals(loadFile(name + ".txt"), result.trim());
    }

    @Test
    public void testSimple(TestInfo testInfo) throws IOException {
        doTest(testInfo);
    }

    private String loadFile(String name) throws IOException {
        String path = getDirPath() + "/" + name;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("No test data: " + path);
            }
            return StringUtil.convertLineSeparators(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).trim();
        }
    }

    /**
     * IntelliJ's {@code getTestName(true)}: the method name without {@code test}, first letter lowercased.
     */
    private static String getTestName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        name = name.startsWith("test") ? name.substring("test".length()) : name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}

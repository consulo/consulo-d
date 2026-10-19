package io.github.intellij.dlanguage.sdlang.parser;

import consulo.language.file.LanguageFileType;
import consulo.test.junit.impl.language.SimpleParsingTest;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.sdlang.SDLangFileType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code SDLangParserTest.kt}. Upstream extends IntelliJ's {@code ParsingTestCase}; like it, source
 * and expected text are trimmed and test names have their first letter lowercased.
 */
public class SDLangParserTest extends SimpleParsingTest<Object> {
    private static final String DATA_PATH = "gold/parser";

    public SDLangParserTest() {
        super(DATA_PATH, "sdl");
    }

    @Test
    public void testSimple(Context context) throws Exception {
        doTest(context);
    }

    private void doTest(Context context) throws Exception {
        doTest(context, null);
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return SDLangFileType.INSTANCE;
    }

    @Override
    protected String getFileName(Context context, @Nullable Object testContext) {
        String name = context.testInfo().getTestMethod().orElseThrow().getName();
        return StringUtil.decapitalize(name.startsWith("test") ? name.substring("test".length()) : name);
    }

    @Override
    protected String loadText(Context context, @Nullable Object testContext, String extension) throws Exception {
        String path = "/" + DATA_PATH + "/" + getFileName(context, testContext) + "." + extension;
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                return "";
            }
            return StringUtil.convertLineSeparators(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).trim();
        }
    }
}

package io.github.intellij.dlanguage.parser;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionUtil;
import consulo.test.junit.impl.language.SimpleParsingTest;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.fileType.FileType;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.documentation.DDocLanguage;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code DDocParserTest.kt}. Upstream is an IntelliJ {@code ParsingTestCase} on the
 * {@code DDocParserDefinition}, with test names starting lowercase ({@code testSimple} reads {@code simple.d}) and
 * the expected tree next to the source ({@code gold/ddoc/parser/simple.txt}). Here the file is created in the
 * {@code DDoc} language, whose parser definition the light application registers.
 */
public class DDocParserTest extends SimpleParsingTest<Object> {
    private static final String DATA_PATH = "gold/ddoc/parser";

    public DDocParserTest() {
        super(DATA_PATH, "d");
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return DlangFileType.INSTANCE;
    }

    @Override
    @RequiredReadAction
    protected PsiFile createFile(Context context, @Nullable Object testContext, String fileName, FileType fileType, String text) {
        LanguageVersion languageVersion = LanguageVersionUtil.findDefaultVersion(DDocLanguage.INSTANCE);
        return context.psiFileFactory().createFileFromText(fileName, DDocLanguage.INSTANCE, languageVersion, text);
    }

    @Override
    protected String getFileName(Context context, @Nullable Object testContext) {
        String name = context.testInfo().getTestMethod().orElseThrow().getName();
        name = name.startsWith("test") ? name.substring("test".length()) : name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
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

    private void doTest(Context context) throws Exception {
        //noinspection RequiredXAction
        PsiFile file = loadPsiFile(context, null);
        checkResult(context, null, file);
        Assertions.assertFalse(
            toParseTreeText(file, skipSpaces(), includeRanges()).contains("PsiErrorElement"),
            "PsiFile contains error elements"
        );
    }

    @Test
    public void testSimple(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testEmbeddedCode(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testEmbeddedCode2(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testEmbeddedCode3(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testEmbeddedHTML(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testEmphasis(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testHorizontalRules(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testInlineCode(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testLinks(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testLinks2(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testListsOrdered(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testListsUnordered(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testListsUnordered2(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testListsUnordered3(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testMarkdownHeading(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testParamsSection(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testQuotes(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testFull(Context context) throws Exception {
        doTest(context);
    }

    @Test
    public void testWrongImageDetection(Context context) throws Exception {
        doTest(context);
    }
}

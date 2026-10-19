package io.github.intellij.dlanguage.parser;

import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.test.junit.impl.language.SimpleParsingTest;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DlangFileType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Port of upstream {@code DLanguageParserTestBase.kt}. Upstream extends IntelliJ's {@code ParsingTestCase}; this one
 * extends Consulo's {@link SimpleParsingTest} and keeps upstream's data layout: sources in {@code gold/<dataPath>},
 * expected trees in {@code gold/<dataPath>/expected}. Like {@code ParsingTestCase}, source and expected text are
 * trimmed and test names keep their case.
 */
public abstract class DLanguageParserTestBase extends SimpleParsingTest<Object> {
    private final String myDataPath;
    private final String myFileExt;

    protected DLanguageParserTestBase(String dataPath, String fileExt) {
        super("gold/" + dataPath, fileExt);
        myDataPath = "gold/" + dataPath;
        myFileExt = fileExt;
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return DlangFileType.INSTANCE;
    }

    @Override
    protected boolean skipSpaces() {
        return true;
    }

    @Override
    protected String getFileName(Context context, @Nullable Object testContext) {
        String name = context.testInfo().getTestMethod().orElseThrow().getName();
        return name.startsWith("test") ? name.substring("test".length()) : name;
    }

    @Override
    protected String loadText(Context context, @Nullable Object testContext, String extension) throws Exception {
        String fileName = getFileName(context, testContext);
        String path = extension.endsWith("txt")
            ? "/" + myDataPath + "/expected/" + fileName + ".txt"
            : "/" + myDataPath + "/" + fileName + "." + extension;
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                return "";
            }
            return StringUtil.convertLineSeparators(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).trim();
        }
    }

    protected void doDlangParserTest(Context context) throws Exception {
        doDlangParserTest(context, true, true);
    }

    /**
     * Perform a test. Add tests that should work but does not work yet with
     * doTestModule(false, false).
     */
    protected void doDlangParserTest(Context context, boolean checkResult, boolean shouldPass) throws Exception {
        //noinspection RequiredXAction
        PsiFile file = loadPsiFile(context, null);
        if (checkResult) {
            checkResult(context, null, file);
        }
        if (shouldPass) {
            Assertions.assertFalse(
                toParseTreeText(file, skipSpaces(), includeRanges()).contains("PsiErrorElement"),
                "PsiFile contains error elements"
            );
        }
    }
}

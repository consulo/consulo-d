package io.github.intellij.dlanguage.folding;

import consulo.application.ReadAction;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.util.TextRange;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.folding.FoldingBuilder;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.editor.folding.LanguageFolding;
import consulo.language.psi.PsiFile;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DFoldingBuilderTest.kt}, which runs IntelliJ's {@code CodeInsightTestFixture.testFolding}. That
 * check is reproduced here without an editor: the test data is written to the project without its {@code <fold>}
 * markup, the regions the editor's folding model would accept (non-empty, not crossing one another) are built from the
 * registered folding builders, and the text is marked up again from them the way
 * {@code CodeInsightTestFixtureImpl.getTagsFromSegments} does, to be compared with the test data.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DFoldingBuilderTest {
    private static final String FOLD = "fold";

    private void doTest(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        String fileName = camelOrWordsToSnake(getTestName(testInfo)) + ".d";
        testFolding(fixture, fileName, loadFile(fileName));
    }

    @Test
    public void testBlock(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testClass(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testComments(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testEnum(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testExtern(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testFunction(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testImport(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testOneLinerFunction(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testStruct(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testVersion(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testOneLinerImport(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testStaticForeach(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testStaticIf(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    @Test
    public void testScopeStatement(CodeInsightTestFixture fixture, TestInfo testInfo) throws Exception {
        doTest(fixture, testInfo);
    }

    private void testFolding(CodeInsightTestFixture fixture, String fileName, String expectedContent) throws Exception {
        String expected = StringUtil.replace(expectedContent, "\r", "");
        String cleanContent = removeFoldingMarkers(expected);

        PsiFile file = fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/" + fileName, cleanContent);
        String actual = ReadAction.compute(() -> {
            Document document = Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(file.getVirtualFile()));
            FoldingBuilder builder = Objects.requireNonNull(FoldingBuilder.forLanguageComposite(DLanguage.INSTANCE));

            List<TextRange> ranges = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            for (FoldingDescriptor descriptor : LanguageFolding.buildFoldingDescriptors(builder, file, document, false)) {
                TextRange range = descriptor.getRange();
                if (range.isEmpty() || ranges.stream().anyMatch(other -> areConflicting(range, other))) {
                    continue;
                }
                ranges.add(range);
                placeholders.add(StringUtil.notNullize(descriptor.getPlaceholderText(), "..."));
            }
            return getFoldingData(cleanContent, ranges, placeholders);
        });

        assertEquals(expected, actual, fileName);
    }

    private static boolean areConflicting(TextRange range1, TextRange range2) {
        if (range1.equals(range2)) {
            return true;
        }
        if (range1.contains(range2) || range2.contains(range1)) {
            return false;
        }
        TextRange intersection = range1.intersection(range2);
        return intersection != null && !intersection.isEmpty();
    }

    private record Border(boolean isLeftBorder, int offset, String text) {
    }

    private static String getFoldingData(String text, List<TextRange> ranges, List<String> placeholders) {
        List<Border> borders = new ArrayList<>();
        for (int i = 0; i < ranges.size(); i++) {
            borders.add(new Border(true, ranges.get(i).getStartOffset(), "text='" + placeholders.get(i) + "'"));
            borders.add(new Border(false, ranges.get(i).getEndOffset(), ""));
        }
        borders.sort(Comparator.comparingInt(Border::offset).thenComparing(Border::text).reversed());
        StringBuilder result = new StringBuilder(text);
        for (Border border : borders) {
            StringBuilder info = new StringBuilder();
            info.append('<');
            if (border.isLeftBorder()) {
                info.append(FOLD).append(' ').append(border.text());
            }
            else {
                info.append('/').append(FOLD);
            }
            info.append('>');
            result.insert(border.offset(), info);
        }
        return result.toString();
    }

    private static String removeFoldingMarkers(String expectedContent) {
        return expectedContent.replaceAll("<" + FOLD + "\\stext='[^']*'(\\sexpand='[^']*')*>", "")
            .replace("</" + FOLD + ">", "");
    }

    private String loadFile(String name) throws Exception {
        String path = "gold/folding/" + name;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String camelOrWordsToSnake(String name) {
        if (name.contains(" ")) {
            return name.replace(" ", "_");
        }
        List<String> parts = new ArrayList<>();
        for (String part : name.split("(?=[A-Z])")) {
            parts.add(part.toLowerCase());
        }
        return String.join("_", parts);
    }

    /**
     * IntelliJ's {@code getTestName(true)}: the method name without {@code test}, its first letter lowercased.
     */
    private static String getTestName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        if (name.startsWith("test")) {
            name = name.substring("test".length());
        }
        return name.isEmpty() ? name : Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}

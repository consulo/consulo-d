package io.github.intellij.dlanguage.annotator;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.localize.LocalizeKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DHighlightingAnnotatorTest.kt}: {@code testHighlighting(false, false, false)} checks the
 * errors marked up in the file, as {@link CodeInsightTestFixture#checkHighlighting()} does.
 * <p>
 * consulo-it loads no localization, so a message is the text of its {@link LocalizeKey}. The {@code descr} of the
 * markup, upstream's English text, is replaced by the key whose {@code en_US} text it is (read from
 * {@code consulo.d.DLocalize.yaml}) before the file is configured.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DHighlightingAnnotatorTest {
    private static final String LOCALIZE_ID = "consulo.d.DLocalize";

    private String getTestDataPath() {
        return "gold/highlighting/annotator";
    }

    // TODO re enable this test
    //@Test
    //fun testTypeParameters() {
    //    myFixture.configureByFile("type_parameters.d")
    //    myFixture.testHighlighting(false, true, false)
    //}


    @Test
    public void testInvalidDelimiterString(CodeInsightTestFixture fixture) throws Exception {
        configureByFile(fixture, "invalid_string_delimiters.d");
        fixture.checkHighlighting();
    }

    @Test
    public void testUnclosedComment(CodeInsightTestFixture fixture) throws Exception {
        configureByFile(fixture, "unclosed_comment.d");
        fixture.checkHighlighting();
    }

    @Test
    public void testInvalidLiteralValues(CodeInsightTestFixture fixture) throws Exception {
        configureByFile(fixture, "invalid_literal_values.d");
        fixture.checkHighlighting();
    }

    @Test
    public void testInvalidLEnums(CodeInsightTestFixture fixture) throws Exception {
        configureByFile(fixture, "invalid_enums.d");
        fixture.checkHighlighting();
    }

    private void configureByFile(CodeInsightTestFixture fixture, String fileName) throws Exception {
        String text;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(getTestDataPath() + "/" + fileName)) {
            assertNotNull(stream, getTestDataPath() + "/" + fileName);
            text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        for (Map.Entry<String, String> entry : loadLocalizeTexts().entrySet()) {
            String escaped = entry.getValue().replace("\"", "\\\"");
            text = text.replace("descr=\"" + escaped + "\"", "descr=\"" + LocalizeKey.of(LOCALIZE_ID, entry.getKey()) + "\"");
        }
        fixture.configureByText(fileName, text);
    }

    /**
     * The {@code key: text} pairs of the plugin's {@code en_US} localize file.
     */
    private Map<String, String> loadLocalizeTexts() throws Exception {
        Map<String, String> texts = new LinkedHashMap<>();
        String path = "LOCALIZE-LIB/en_US/" + LOCALIZE_ID + ".yaml";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String key = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(" ") && line.endsWith(":")) {
                    key = line.substring(0, line.length() - 1);
                }
                else if (key != null && line.trim().startsWith("text: ")) {
                    texts.put(key, line.trim().substring("text: ".length()));
                    key = null;
                }
            }
        }
        return texts;
    }
}

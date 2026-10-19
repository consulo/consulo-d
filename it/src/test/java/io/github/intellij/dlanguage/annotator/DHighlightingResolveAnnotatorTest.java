package io.github.intellij.dlanguage.annotator;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.it.UseInspection;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.rawHighlight.HighlightInfo;
import io.github.intellij.dlanguage.inspections.PossiblyUndefinedSymbol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Represents tests for highlighting due to unresolved symbols
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DHighlightingResolveAnnotatorTest {

    /**
     * Upstream's {@code testHighlighting(checkWarnings = true, checkInfos = false, checkWeakWarnings = true)} on a file
     * without markup fails on any error, warning or weak warning; {@link CodeInsightTestFixture#checkHighlighting()}
     * only checks errors there, so the warnings are asserted on directly.
     */
    @Test
    @UseInspection(PossiblyUndefinedSymbol.class)
    public void testNamedImportShouldNotBeUndefined(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/foo.d", "void test() {}");
        fixture.configureByText("main.d", "import foo: bar = test;");

        Set<HighlightSeverity> checked = Set.of(HighlightSeverity.ERROR, HighlightSeverity.WARNING, HighlightSeverity.WEAK_WARNING);
        List<String> problems = fixture.doHighlighting().stream()
            .filter(info -> checked.contains(info.getSeverity()))
            .map(DHighlightingResolveAnnotatorTest::describe)
            .toList();
        assertEquals(List.of(), problems);
    }

    private static String describe(HighlightInfo info) {
        return info.getSeverity() + " " + info.getStartOffset() + ".." + info.getEndOffset() + ": "
            + (info.getDescription() == null ? null : info.getDescription().get());
    }
}

package io.github.intellij.dub.codeinsight.linemarker;

import consulo.application.ReadAction;
import consulo.document.Document;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.gutter.LineMarkerInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samael Bate (singingbush)
 * created on 19/07/2022
 * <p>
 * Port of upstream {@code DubExecutableRunLineMarkerContributorTest.kt}, whose {@code @Test} upstream keeps commented
 * out. The gutters are the markers of the line marker pass, those at the caret the ones on the caret's line. Upstream's
 * checks of IntelliJ's {@code RunLineMarkerProvider.hadAnythingRunnable} are left out: Consulo has no such state.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DubExecutableRunLineMarkerContributorTest {

    @Test
    @DisplayName("test line marker detects main function")
    public void testLineMarkerDetectsMainFunction(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("app.d",
            """
                import std.stdio : writeln;
                void <caret>main() {
                    writeln("Hello World!");
                }"""
        );

        List<LineMarkerInfo<?>> gutters = fixture.getLineMarkers();
        Document document = fixture.getDocument();
        int caretLine = ReadAction.compute(() -> document.getLineNumber(fixture.getExpectedHighlightingData().getCaretOffset()));
        long guttersAtCaret = ReadAction.compute(() -> gutters.stream()
            .filter(gutter -> document.getLineNumber(gutter.startOffset) == caretLine)
            .count());

        // the <caret> is on the line that has "void main()" so a gutter should be found on that line
        assertEquals(1, guttersAtCaret);

        // there should only be one entry point in the source, so only one gutter line marker in the file
        assertEquals(1, gutters.size());
    }
}

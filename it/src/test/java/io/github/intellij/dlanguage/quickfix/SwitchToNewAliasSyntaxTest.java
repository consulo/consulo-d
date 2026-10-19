package io.github.intellij.dlanguage.quickfix;

import consulo.it.CodeInsightTestFixture;
import consulo.it.UseInspection;
import io.github.intellij.dlanguage.inspections.OldAliasSyntax;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Port of upstream {@code SwitchToNewAliasSyntaxTest.kt}, which upstream keeps commented out
 * ("todo: Stop this test timing out!").
 */
public class SwitchToNewAliasSyntaxTest extends DQuickFixTestBase {

    @Test
    @Disabled("consulo-it loads no LOCALIZE-LIB, so the warning's message is its key")
    @UseInspection(OldAliasSyntax.class)
    @DisplayName("test inspection warning for alias syntax")
    public void testInspectionWarningForAliasSyntax(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "warningSwitchToNewAliasSyntax.d");
    }

    @Test
    @Disabled("consulo-it loads no LOCALIZE-LIB, so the fix's text is its key; applying the fix also logs errors of the"
        + " headless platform (ScratchFileService is not bound, MarkerCache assertion in the PSI-to-document sync)")
    @UseInspection(OldAliasSyntax.class)
    @DisplayName("test quickfix for alias syntax")
    public void testQuickfixForAliasSyntax(CodeInsightTestFixture fixture) throws Exception {
        doSingleTest(fixture, "SwitchToNewAlias.d");
    }
}

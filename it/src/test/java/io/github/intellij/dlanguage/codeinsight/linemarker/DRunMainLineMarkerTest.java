package io.github.intellij.dlanguage.codeinsight.linemarker;

import consulo.application.util.ApplicationUtil;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.gutter.LineMarkerInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Port of upstream {@code DRunMainLineMarkerTest.kt}. Upstream reads the editor's line markers after highlighting; here
 * the line marker pass is asked for them directly. Upstream also checks that a run marker was offered, which Consulo
 * does not record.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DRunMainLineMarkerTest {

    private void doTest(CodeInsightTestFixture fixture, String fileContent, boolean shouldFind) throws Exception {
        fixture.configureByText("test.d", fileContent);
        doHighlighting(fixture);
        List<LineMarkerInfo<?>> markers = fixture.getLineMarkers();
        if (shouldFind) {
            assertEquals(1, markers.size());
        }
        else {
            assertEquals(0, markers.size());
        }
    }

    /**
     * consulo-it's highlighting fails instead of restarting when a write action interrupts its read action
     * ({@code CannotRunReadActionException}), as the daemon would; retried here until the fixture does it.
     */
    private static void doHighlighting(CodeInsightTestFixture fixture) throws Exception {
        for (int attempt = 1; ; attempt++) {
            try {
                fixture.doHighlighting();
                return;
            }
            catch (AssertionError e) {
                if (attempt == 3 || !(e.getCause() instanceof ApplicationUtil.CannotRunReadActionException)) {
                    throw e;
                }
            }
        }
    }

    @Test
    @DisplayName("test should find basic main")
    public void testShouldFindBasicMain(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            void main() {}
            """, true);
    }

    @Test
    @DisplayName("test should find basic main no args return")
    public void testShouldFindBasicMainNoArgsReturn(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            int main() {}
            """, true);
    }

    @Test
    @DisplayName("test should find basic main args")
    public void testShouldFindBasicMainArgs(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            void main(string[] args) {}
            """, true);
    }

    @Test
    @DisplayName("test should find basic main args auto return")
    public void testShouldFindBasicMainArgsAutoReturn(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            auto main(string[] args) {}
            """, true);
    }

    @Test
    @DisplayName("test should find basic noreturn return")
    public void testShouldFindBasicNoreturnReturn(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            noreturn main() { while (true) ;}
            """, true);
    }

    @Test
    @DisplayName("test should not find runable method")
    public void testShouldNotFindRunableMethod(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            void foo() {}
            """, false);
    }

    @Test
    @DisplayName("test should not find invalid main")
    public void testShouldNotFindInvalidMain(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            string main() {return "";}
            """, false);
    }

    @Test
    @DisplayName("test should not find c main")
    public void testShouldNotFindCMain(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            int main(int argc, char** argv) {return 0;}
            """, false);
    }

    @Test
    @DisplayName("test should not find invalid main casing")
    public void testShouldNotFindInvalidMainCasing(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            void Main() {}
            """, false);
    }

    @Test
    @DisplayName("test should not find inner main method")
    public void testShouldNotFindInnerMainMethod(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            class A {
                void main(string args) {}
            }
            """, false);
    }

    @Test
    @DisplayName("test should not find inner main method 2")
    public void testShouldNotFindInnerMainMethod2(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            fun A() {
                void main(string args) {}
            }
            """, false);
    }

    @Test
    @DisplayName("test should not find invalid main argument type")
    public void testShouldNotFindInvalidMainArgumentType(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            void main(string args) {}
            """, false);
    }

    @Test
    @DisplayName("test should not find incomplete main")
    public void testShouldNotFindIncompleteMain(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, """
            module test;

            void main
            """, false);
    }
}

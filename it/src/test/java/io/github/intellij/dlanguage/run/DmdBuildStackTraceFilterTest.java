package io.github.intellij.dlanguage.run;

import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of upstream {@code DmdBuildStackTraceFilterTest.kt}.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DmdBuildStackTraceFilterTest {
    @Test
    public void testLineMatchCorrectlyMatching() {
        assertTrue(DmdConsoleFilter.getPattern().matcher("/home/user/project/source/main.d(135): Error: declaration `main.main.x` is already defined").find());
        assertTrue(DmdConsoleFilter.getPattern().matcher("/home/user/project/source/main.d(130):        `variable` `x` is defined here").find());
    }

    @Test
    public void testFindCorrectElements() {
        final Matcher found = DmdConsoleFilter.getPattern().matcher("/home/user/project/source/main.d(135): Error: declaration `main.main.x` is already defined");
        assertTrue(found.find());
        assertEquals(3, found.groupCount() + 1);
        assertEquals("/home/user/project/source/main.d".length(), found.end(1));
        assertEquals("135", found.group(2));
    }

    @Test
    public void testApplyFilter(Project project) {
        final String line = "/home/user/project/source/main.d(135): Error: declaration `main.main.x` is already defined";
        assertDoesNotThrow(() -> new DmdConsoleFilter(project, GlobalSearchScope.allScope(project)).applyFilter(line, line.length()));
    }
}

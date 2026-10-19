package io.github.intellij.dub.run;

import consulo.project.Project;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ConsuloProjectLoader.class)
public class DubBuildSourceFileFilterTest {
    @Test
    public void testLineMatchCorrectlyMatching() {
        assertTrue(DubBuildSourceFileFilter.D_SOURCE_PATH_FORMAT.matcher("source\\package\\app.d Some Log output").matches());
        // Windows separators
        assertTrue(DubBuildSourceFileFilter.D_SOURCE_PATH_FORMAT.matcher("source\\package\\app.d Some Log output\n").matches());
        // Unix separators
        assertTrue(DubBuildSourceFileFilter.D_SOURCE_PATH_FORMAT.matcher("source/package/app.d Some Log output\n").matches());

    }

    @Test
    public void testTheRegexForSplittingADSourcePathLineNoAndMessage() {
        assertEquals("source\\package\\app.d",
            group("source\\package\\app.d Some Log output", 1)
        );

        assertEquals("",
            group("source\\package\\app.d Some Log output", 2)
        );

        assertEquals("Some Log output",
            group("source\\package\\app.d Some Log output", 3)
        );


        assertEquals("source\\package\\app.d",
            group("source\\package\\app.d(53,31) Some Log output", 1)
        );

        assertEquals("(53,31)",
            group("source\\package\\app.d(53,31) Some Log output", 2)
        );

        assertEquals("Some Log output",
            group("source\\package\\app.d(53,31) Some Log output", 3)
        );


        assertEquals("source\\package\\app.di",
            group("source\\package\\app.di(53,31): Some Log output", 1)
        );

        assertEquals("(53,31)",
            group("source\\package\\app.di(53,31): Some Log output", 2)
        );

        assertEquals("Some Log output",
            group("source\\package\\app.di(53,31): Some Log output", 3)
        );


        assertEquals("source\\package\\app.d",
            group("source\\package\\app.d:53 Some Log output", 1)
        );

        assertEquals("53",
            group("source\\package\\app.d:53 Some Log output", 2)
        );

        assertEquals("Some Log output",
            group("source\\package\\app.d:53 Some Log output", 3)
        );
    }


    @Test
    public void testFilteringDubOutputForSourceFile() {
        String line = "source\\config\\database.d(53,31):    blah blah blah, some other stuff";

        // applyFilter resolves the path through LocalFileSystem, which the light test application does not provide
        assertEquals("source\\config\\database.d",
            group(line, 1)
        );
        assertEquals("(53,31)",
            group(line, 2)
        );
        assertEquals("blah blah blah, some other stuff",
            group(line, 3)
        );
    }

    /*
     * This test was added due to error report on Sentry.io
     * User got an IndexOutOfBoundsException for dub output that isn't expected
     */
    @Test
    public void testCanHandleUnexpectedInputIssue914(Project project) {
        String line = "00:53:21.770 2> -break-insert -f C:/my/project/source/app.d:7";

        DubBuildSourceFileFilter filter = new DubBuildSourceFileFilter(project);

        assertDoesNotThrow(() -> filter.applyFilter(line, line.length()));

        assertNull(filter.applyFilter(line, line.length()));
    }

    @Test
    public void testFilteringDubOutputForLinkerErrorIssue625() {
        String line = "source/app.d:21 _D3std9exception__T7bailOutHTC9ExceptionZQwFNaNfAyamMAxaZNn [0x10c0aa25a]";

        // applyFilter resolves the path through LocalFileSystem, which the light test application does not provide
        assertEquals("source/app.d",
            group(line, 1)
        );
        assertEquals("21",
            group(line, 2)
        );
        assertEquals("_D3std9exception__T7bailOutHTC9ExceptionZQwFNaNfAyamMAxaZNn [0x10c0aa25a]",
            group(line, 3)
        );
    }

    @Test
    public void testFilteringDubOutputWithoutSourceFileNoColon(Project project) {
        String line = "blah blah blah, some kind of output";

        DubBuildSourceFileFilter filter = new DubBuildSourceFileFilter(project);

        assertNull(filter.applyFilter(line, line.length()), "shouldn't apply filter if no source found");
    }

    @Test
    public void testFilteringDubOutputWithSomeFilePathInside(Project project) {
        String line = "Starting process 'command 'rdmd''. Working directory: /path/to/intellij/intellij-dlanguage/gen/io/github/intellij/dlanguage/psi Command: rdmd types_regen_script.d\n";

        DubBuildSourceFileFilter filter = new DubBuildSourceFileFilter(project);

        assertNull(filter.applyFilter(line, line.length()), "shouldn't apply filter if no source found");
    }

    private static String group(String input, int group) {
        Matcher matcher = DubBuildSourceFileFilter.D_SOURCE_PATH_FORMAT.matcher(input);
        assertTrue(matcher.find());
        String value = matcher.group(group);
        return value == null ? "" : value;
    }
}

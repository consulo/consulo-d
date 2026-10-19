package io.github.intellij.dub.run;

import consulo.execution.ui.console.Filter;
import consulo.execution.ui.console.OpenFileHyperlinkInfo;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Will get a match on output that contains a path to a D source file and make the filepath a clickable link.
 * The filter checks for the line to start with "source" as it's more likely to be a project source file.
 */
public class DubBuildSourceFileFilter implements Filter {
    // This regex will get a match on any of the following:
    //     source\package\app.d Some Log output
    //     source\package\app.d(53,31) Some Log output
    //     source\package\app.di(53,31): Some Log output
    //     source\package\app.di Some Log output
    //     source\path\app.d:21 Some text output
    static final Pattern D_SOURCE_PATH_FORMAT = Pattern.compile("^(.*\\.di?):?(\\(\\d+,\\d+\\)|\\d+)?:?\\s+(.*\\n?)$");

    private final Project myProject;

    public DubBuildSourceFileFilter(Project project) {
        myProject = project;
    }

    @Override
    public @Nullable Result applyFilter(String line, int entireLength) {
        List<String> found = new ArrayList<>();
        for (String it : line.split("\n", -1)) {
            Matcher matcher = D_SOURCE_PATH_FORMAT.matcher(it);
            if (matcher.find()) {
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    String group = matcher.group(i);
                    found.add(group == null ? "" : group);
                }
            }
        }
        if (!found.isEmpty()) {
            // then it's certain we can get a hyperlink to the source file and possibly the line number
            List<String> groups = found.subList(1, found.size()); // the first one will be entire string

            if (groups.isEmpty()) {
                // This shouldn't really happen but there have been IndexOutOfBoundsException errors in sentry occasionally
                // so best to be defensive
                return null;
            }

            VirtualFile file = LocalFileSystem
                .getInstance()
                .findFileByPath(myProject.getBasePath() + "/" + groups.get(0).replace("\\", "/"));

            // negate 1 from the line number as IDEA does line numbers starting from
            int lineNumber = !groups.get(1).isBlank() ? parseLineNumber(groups.get(1)) - 1 : 0;
            int columnNumber = !groups.get(1).isBlank() ? parseColumnNumber(groups.get(1)) - 1 : 0;

            int offset = entireLength - line.length();
            if (file != null) {
                return new Result(offset, offset + groups.get(0).length(),
                    new OpenFileHyperlinkInfo(myProject, file, lineNumber, columnNumber)
                );
            }
        }
        return null;
    }

    private static int parseLineNumber(String text) {
        String number;
        if (text.startsWith("(")) {
            number = substringBefore(substringAfter(text, "("), ")")
                .split(",")[0]; // when the test is "(23,25)" it's line number followed by column. We don't need the column
        }
        else {
            number = text;
        }

        try {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException e) {
            return 0; // zero is fine as a default line number, it'll just mean linking to the source file rather than exact line
        }
    }

    private static int parseColumnNumber(String text) {
        String number;
        if (text.startsWith("(")) {
            number = substringBefore(substringAfter(text, "("), ")")
                .split(",")[1]; // when the test is "(23,25)" it's line number followed by column. We need the column
        }
        else {
            return 0;
        }

        try {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException e) {
            return 0; // zeros is fin as default line number
        }
    }

    private static String substringAfter(String text, String delimiter) {
        int index = text.indexOf(delimiter);
        return index == -1 ? text : text.substring(index + delimiter.length());
    }

    private static String substringBefore(String text, String delimiter) {
        int index = text.indexOf(delimiter);
        return index == -1 ? text : text.substring(0, index);
    }
}

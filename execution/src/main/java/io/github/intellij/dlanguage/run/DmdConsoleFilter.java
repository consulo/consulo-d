package io.github.intellij.dlanguage.run;

import consulo.content.scope.SearchScope;
import consulo.execution.ui.console.Filter;
import consulo.execution.ui.console.OpenFileHyperlinkInfo;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of {@code DmdConsoleFilter} from upstream {@code DmdConsoleFilterProvider.kt}.
 */
public class DmdConsoleFilter implements Filter {
    private static final Pattern PATTERN = Pattern.compile("(\\.di?)\\((\\d+)\\):");

    private final Project myProject;
    private final SearchScope myScope;

    public DmdConsoleFilter(Project project, SearchScope scope) {
        myProject = project;
        myScope = scope;
    }

    public static Pattern getPattern() {
        return PATTERN;
    }

    public Project getProject() {
        return myProject;
    }

    public SearchScope getScope() {
        return myScope;
    }

    @Override
    public @Nullable Result applyFilter(String line, int entireLength) {
        Matcher messageSuffix = PATTERN.matcher(line);
        if (!messageSuffix.find()) {
            return null;
        }
        int pathEnd = messageSuffix.end(1);
        String path = line.substring(0, pathEnd);
        int lineNumber = Integer.parseInt(messageSuffix.group(2)) - 1;

        int offset = entireLength - line.length();
        VirtualFile file = LocalFileSystem.getInstance().findFileByPathIfCached(path);
        if (file == null) {
            return null;
        }
        return new Result(offset, offset + messageSuffix.end() - 1, new OpenFileHyperlinkInfo(myProject, file, lineNumber, 0));
    }
}

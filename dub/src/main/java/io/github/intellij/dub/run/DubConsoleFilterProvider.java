package io.github.intellij.dub.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.ui.console.ConsoleFilterProvider;
import consulo.execution.ui.console.Filter;
import consulo.project.Project;

@ExtensionImpl
public class DubConsoleFilterProvider implements ConsoleFilterProvider {
    @Override
    public Filter[] getDefaultFilters(Project project) {
        return new Filter[] {new DubBuildSourceFileFilter(project)};
    }
}

package io.github.intellij.dlanguage.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.scope.SearchScope;
import consulo.execution.ui.console.ConsoleFilterProviderEx;
import consulo.execution.ui.console.Filter;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

/**
 * Port of upstream {@code DmdConsoleFilterProvider.kt} (its {@code DmdConsoleFilter} is {@link DmdConsoleFilter}).
 */
@ExtensionImpl
public class DmdConsoleFilterProvider implements ConsoleFilterProviderEx {

    @Override
    public Filter[] getDefaultFilters(Project project) {
        return getDefaultFilters(project, GlobalSearchScope.allScope(project));
    }

    @Override
    public Filter[] getDefaultFilters(Project project, SearchScope scope) {
        return new Filter[] {new DmdConsoleFilter(project, scope)};
    }
}

package io.github.intellij.dlanguage.features;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.ide.navigation.GotoSymbolContributor;
import consulo.language.psi.search.FindSymbolParameters;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.navigation.NavigationItem;
import consulo.project.content.scope.ProjectAwareSearchScope;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.stubs.index.DAllNameIndex;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * The "go to symbol"
 * <p>
 * Consulo's contributors pass names and items to a processor instead of returning arrays, and the scope (project or
 * all, upstream's {@code includeNonProjectItems}) comes with the request.
 */
@ExtensionImpl
public class DChooseByNameContributor implements GotoSymbolContributor {
    @Override
    public void processNames(Processor<String> processor, SearchScope scope, @Nullable IdFilter filter) {
        StubIndex.getInstance().processAllKeys(DAllNameIndex.KEY, processor, (ProjectAwareSearchScope) scope, filter);
    }

    @Override
    public void processElementsWithName(String name, Processor<NavigationItem> processor, FindSymbolParameters parameters) {
        Collection<DNamedElement> classes = StubIndex.getElements(DAllNameIndex.KEY, name, parameters.getProject(), parameters.getSearchScope(), DNamedElement.class);
        for (DNamedElement item : classes) {
            if (!processor.process(item)) {
                return;
            }
        }
    }
}

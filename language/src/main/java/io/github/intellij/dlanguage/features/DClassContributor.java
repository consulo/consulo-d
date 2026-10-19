package io.github.intellij.dlanguage.features;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.ide.navigation.GotoClassOrTypeContributor;
import consulo.language.psi.search.FindSymbolParameters;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.navigation.NavigationItem;
import consulo.project.content.scope.ProjectAwareSearchScope;
import io.github.intellij.dlanguage.psi.impl.named.DlangClassDeclarationImpl;
import io.github.intellij.dlanguage.psi.impl.named.DlangEnumDeclarationImpl;
import io.github.intellij.dlanguage.psi.impl.named.DlangInterfaceDeclarationImpl;
import io.github.intellij.dlanguage.psi.impl.named.DlangStructDeclarationImpl;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.stubs.index.DAllNameIndex;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * The "go to class"
 * <p>
 * Consulo's contributors pass names and items to a processor instead of returning arrays, and the scope (project or
 * all, upstream's {@code includeNonProjectItems}) comes with the request.
 */
@ExtensionImpl
public class DClassContributor implements GotoClassOrTypeContributor {
    @Override
    public void processNames(Processor<String> processor, SearchScope scope, @Nullable IdFilter filter) {
        StubIndex.getInstance().processAllKeys(DAllNameIndex.KEY, processor, (ProjectAwareSearchScope) scope, filter);
    }

    @Override
    public void processElementsWithName(String name, Processor<NavigationItem> processor, FindSymbolParameters parameters) {
        Collection<DNamedElement> result = StubIndex.getElements(DAllNameIndex.KEY, name, parameters.getProject(), parameters.getSearchScope(), DNamedElement.class);
        for (DNamedElement element : result) {
            if (element.getClass() == DlangClassDeclarationImpl.class || element.getClass() == DlangInterfaceDeclarationImpl.class
                || element.getClass() == DlangEnumDeclarationImpl.class || element.getClass() == DlangStructDeclarationImpl.class) {
                if (!processor.process(element)) {
                    return;
                }
            }
        }
    }
}

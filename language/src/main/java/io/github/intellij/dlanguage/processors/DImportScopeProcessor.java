package io.github.intellij.dlanguage.processors;

import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by francis on 6/16/2017.
 */
public class DImportScopeProcessor implements PsiScopeProcessor {

    private final Set<DLanguageSingleImport> myImports = new LinkedHashSet<>();
    private final Set<DLanguageSingleImport> myPublicImports = new LinkedHashSet<>();

    public Set<DLanguageSingleImport> getImports() {
        return myImports;
    }

    public Set<DLanguageSingleImport> getPublicImports() {
        return myPublicImports;
    }

    @Override
    public void handleEvent(Event event, @Nullable Object associated) {
    }

    @Override
    public <T> @Nullable T getHint(Key<T> hintKey) {
        return null;
    }

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (element instanceof DLanguageSingleImport singleImport) {
            myImports.add(singleImport);
        }
        return true;
    }

}

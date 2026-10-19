package io.github.intellij.dlanguage.processors;

import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;
import io.github.intellij.dlanguage.stubs.index.DTopLevelDeclarationsByModule;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by francis on 6/17/2017.
 */

public class DCompletionProcessor implements PsiScopeProcessor {

    private final Set<String> myCompletions = new LinkedHashSet<>();//todo a list might improve performance

    public Set<String> getCompletions() {
        return myCompletions;
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
        if (element instanceof DNamedElement namedElement) {
            String name = namedElement.getName();
            if (name == null) {
                return true;
            }

            if (element instanceof DLanguageFunctionDeclaration) {
                myCompletions.add(name + "(" + ")");
            }
            else {
                myCompletions.add(name);
            }
        }
        if (element instanceof DLanguageSingleImport singleImport) {
            for (DNamedElement it : DTopLevelDeclarationsByModule.getSymbolsFromImport(singleImport)) {
                myCompletions.add(it instanceof DLanguageFunctionDeclaration ? Objects.requireNonNull(it.getName()) + "()" : Objects.requireNonNull(it.getName()));
            }
        }
        return true;
    }


}

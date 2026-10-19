package io.github.intellij.dlanguage.psi.resolve.processor;

import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.collection.SmartList;
import io.github.intellij.dlanguage.psi.DResolveResult;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GenericProcessor implements PsiScopeProcessor {
    private final String myElementName;
    private final @Nullable PsiElement myStartPlace;
    private @Nullable List<DResolveResult> myCandidates = null;
    private ResolveResult @Nullable [] myResolveResult = ResolveResult.EMPTY_ARRAY;

    public GenericProcessor(String elementName, @Nullable PsiElement startPlace) {
        myElementName = elementName;
        myStartPlace = startPlace;
    }

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (!(element instanceof DNamedElement namedElement)) {
            return true;
        }
        if (!myElementName.equals(namedElement.getName())) {
            return true;
        }
        List<DResolveResult> candidates = new SmartList<>();
        candidates.add(new DResolveResult(element));
        myCandidates = candidates;
        myResolveResult = null;
        return false;
    }

    public ResolveResult[] getResult() {
        ResolveResult[] resolveResult = myResolveResult;
        if (resolveResult != null) {
            return resolveResult;
        }
        List<DResolveResult> candidates = myCandidates;
        if (candidates == null) {
            resolveResult = ResolveResult.EMPTY_ARRAY;
        }
        else {
            resolveResult = candidates.toArray(ResolveResult.EMPTY_ARRAY);
        }
        myResolveResult = resolveResult;
        return resolveResult;
    }
}

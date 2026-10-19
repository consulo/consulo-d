package io.github.intellij.dlanguage.psi.resolve.processor;

import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.SmartList;
import io.github.intellij.dlanguage.psi.DLanguageNamedImportBind;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DResolveResult;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.interfaces.TemplateParameter;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageDeclaratorIdentifier;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageModule;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class TypeProcessor implements PsiScopeProcessor {
    private final String myElementName;
    private final PsiElement myStartPLace;
    private final boolean myForceTemplateSearch;
    private final boolean myCanSearchVariable;
    private @Nullable List<DResolveResult> myCandidates = null;
    private ResolveResult @Nullable [] myResolveResult = ResolveResult.EMPTY_ARRAY;

    public TypeProcessor(String elementName, PsiElement startPLace, boolean forceTemplateSearch, boolean canSearchVariable) {
        myElementName = elementName;
        myStartPLace = startPLace;
        myForceTemplateSearch = forceTemplateSearch;
        myCanSearchVariable = canSearchVariable;
    }

    @Override
    public boolean execute(PsiElement element, ResolveState state) {
        if (!(element instanceof UserDefinedType) && !(element instanceof DLanguageDeclaratorIdentifier)
            && !(element instanceof DLanguageAliasInitializer) && !(element instanceof DLanguageTemplateDeclaration)
            && !(element instanceof TemplateParameter) && !(element instanceof DLanguageNamedImportBind)
            && !(element instanceof DLanguageModule) && !(element instanceof DLanguagePackage)
        ) {
            if (!(myCanSearchVariable
                && (element instanceof DLanguageParameter
                || element instanceof DLanguageAutoAssignment
                || element instanceof DLanguageIdentifierInitializer))) {
                return true;
            }
        }
        if (!myElementName.equals(((DNamedElement) element).getName())) {
            return true;
        }
        if (myForceTemplateSearch
            && !(element instanceof DLanguageTemplateDeclaration)
            && PsiTreeUtil.getChildOfType(element, DLanguageTemplateParameters.class) == null) {
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

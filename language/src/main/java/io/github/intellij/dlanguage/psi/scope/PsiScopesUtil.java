package io.github.intellij.dlanguage.psi.scope;

import consulo.application.progress.ProgressIndicatorProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import io.github.intellij.dlanguage.psi.DLanguageAttributeSpecifier;
import io.github.intellij.dlanguage.psi.interfaces.Expression;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageModule;
import io.github.intellij.dlanguage.psi.named.DLanguagePackage;
import io.github.intellij.dlanguage.psi.scope.processor.PackageOrModuleProcessor;
import io.github.intellij.dlanguage.psi.scope.processor.UFCSProcessor;
import io.github.intellij.dlanguage.psi.types.DAliasType;
import io.github.intellij.dlanguage.psi.types.DArrayType;
import io.github.intellij.dlanguage.psi.types.DPointerType;
import io.github.intellij.dlanguage.psi.types.DType;
import io.github.intellij.dlanguage.psi.types.UserDefinedDType;
import io.github.intellij.dlanguage.resolve.DResolveUtilKt;
import org.jspecify.annotations.Nullable;

public final class PsiScopesUtil {
    public static final PsiScopesUtil INSTANCE = new PsiScopesUtil();

    private PsiScopesUtil() {
    }

    public boolean treeWalkUp(PsiScopeProcessor processor, PsiElement entrance, @Nullable PsiElement maxScope) {
        PsiElement scope = entrance;
        PsiElement prevParent = entrance;
        while (scope != null) {
            ProgressIndicatorProvider.checkCanceled();
            if (!scope.processDeclarations(processor, ResolveState.initial(), prevParent, entrance)) {
                return false;
            }
            if (scope.equals(maxScope)) {
                break;
            }
            prevParent = scope;
            scope = scope.getContext();
        }
        return true;
    }

    public boolean walkChildrenScopes(
        PsiElement thisElement,
        PsiScopeProcessor processor,
        ResolveState state,
        @Nullable PsiElement lastParent,
        PsiElement place
    ) {
        PsiElement child = null;

        if (lastParent != null && lastParent.getParent() == thisElement) {
            child = lastParent.getPrevSibling();
            if (child == null) {
                return true; // first element
            }
        }
        if (child == null) {
            child = thisElement.getLastChild();
        }

        while (child != null) {
            if (!child.processDeclarations(processor, state, lastParent, place)) {
                return false;
            }
            child = child.getPrevSibling();
        }

        return true;
    }

    /**
     * Look for attribute specifier in previous siblings
     */
    public boolean searchAttributeSpecifierInPreviousSiblings(
        PsiElement thisElement,
        PsiScopeProcessor processor,
        ResolveState state
    ) {
        PsiElement child = thisElement.getPrevSibling();
        if (child == null) {
            return true;
        }

        while (child != null) {
            // ignore attribute with block (example `public {}`) as they don’t impact us
            if (child instanceof DLanguageAttributeSpecifier attributeSpecifier && attributeSpecifier.getDeclarationBlock() == null) {
                if (!processor.execute(child, state)) {
                    return false;
                }
            }
            child = child.getPrevSibling();
        }

        return true;
    }

    public boolean resolveAndWalk(PsiScopeProcessor processor, PsiQualifiedReference reference, @Nullable PsiElement maxScope) {
        return resolveAndWalk(processor, reference, maxScope, false);
    }

    public boolean resolveAndWalk(
        PsiScopeProcessor processor,
        PsiQualifiedReference reference,
        @Nullable PsiElement maxScope,
        boolean incompleteCode
    ) {
        PsiElement qualifier = reference.getQualifier();
        if (qualifier != null) {
            // composite expression
            DType type = null;
            if (qualifier instanceof Expression expression) {
                type = expression.getDType();
                if (!processTypeDeclaration(type, reference.getElement(), processor)) {
                    return false;
                }
            }
            if (type == null) {
                PsiReference qualifierReference = qualifier.getReference();
                PsiElement target = qualifierReference != null ? qualifierReference.resolve() : null;
                ResolveState state = ResolveState.initial();
                if (target instanceof DLanguagePackage targetPackage) {
                    // First try to resolve to a package or module (of imported symbol)
                    PackageOrModuleProcessor packageProcessor = new PackageOrModuleProcessor(processor, targetPackage);
                    if (!treeWalkUp(packageProcessor, reference.getElement(), null)) {
                        return false;
                    }
                }
                if (target instanceof DLanguagePackage || target instanceof DLanguageModule) {
                    state = state.put(DResolveUtilKt.getIS_QUALIFIED_SYMBOL(), true);
                }
                boolean toContinue = target == null || target.processDeclarations(processor, state, null, reference.getElement());
                if (!toContinue) {
                    return false;
                }
            }
            // UFCS search
            UFCSProcessor ufcsProcessor = new UFCSProcessor(processor);
            return treeWalkUp(ufcsProcessor, reference.getElement(), null);
        }
        else {
            // First try to resolve to a package or module (of imported symbol)
            PackageOrModuleProcessor packageProcessor = new PackageOrModuleProcessor(processor, null);
            if (!treeWalkUp(packageProcessor, reference.getElement(), null)) {
                return false;
            }

            // simple expression -> resolve a top level declaration
            return treeWalkUp(processor, reference.getElement(), maxScope);
        }
    }

    private boolean processTypeDeclaration(@Nullable DType type, PsiElement place, PsiScopeProcessor processor) {
        if (type instanceof DPointerType pointerType) {
            processTypeDeclaration(pointerType.getBase(), place, processor);
        }
        if (type instanceof DArrayType arrayType) {
            processTypeDeclaration(arrayType.getBase(), place, processor);
        }
        if (type instanceof DAliasType aliasType) {
            processTypeDeclaration(aliasType.getAliased(), place, processor);
        }
        if (type instanceof UserDefinedDType userDefinedDType) {
            UserDefinedType target = userDefinedDType.resolve();
            return target.processDeclarations(processor, ResolveState.initial(), null, place);
        }
        return true;
    }
}

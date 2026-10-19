package io.github.intellij.dlanguage.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiQualifiedReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.resolve.processor.TypeProcessor;
import io.github.intellij.dlanguage.psi.scope.PsiScopesUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class TypeResolveUtil {
    public static final TypeResolveUtil INSTANCE = new TypeResolveUtil();

    private TypeResolveUtil() {
    }

    @RequiredReadAction
    public @Nullable DNamedElement resolveType(PsiQualifiedReference reference, boolean forceTemplateSearch, boolean canSearchVariable) {
        return resolveType(reference, forceTemplateSearch, canSearchVariable, false);
    }

    @RequiredReadAction
    public @Nullable DNamedElement resolveType(
        PsiQualifiedReference reference,
        boolean forceTemplateSearch,
        boolean canSearchVariable,
        boolean topLevelOnly
    ) {
        String userTypeName = Objects.requireNonNull(reference.getReferenceName());
        TypeProcessor processor = new TypeProcessor(userTypeName, reference.getElement(), forceTemplateSearch, canSearchVariable);
        if (topLevelOnly) {
            PsiFile containingFile = Objects.requireNonNull(reference.getElement().getContainingFile());
            containingFile.processDeclarations(processor, ResolveState.initial(), containingFile, reference.getElement());
        }
        else {
            PsiScopesUtil.INSTANCE.resolveAndWalk(processor, reference, null);
        }
        ResolveResult[] result = processor.getResult();
        if (result.length == 1) {
            return (DNamedElement) result[0].getElement();
        }
        return null;
    }
}

package consulo.d.language.resolve;

import consulo.project.DumbService;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.application.util.CachedValueProvider;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Resolve results of the D references are kept in {@link ResolveCache}, which every PSI change clears. Its keys are weak
 * and compared by identity of the reference object, so an element hands out the same reference object until its file
 * changes ({@link #reference}). Results computed in dumb mode are not cached: the cache is not cleared when indexing
 * ends.
 */
public final class DResolveCaching {
    private DResolveCaching() {
    }

    public static <R extends PsiReference> @Nullable R reference(PsiElement element, Supplier<@Nullable R> factory) {
        return LanguageCachedValueUtil.getCachedValue(element, () -> CachedValueProvider.Result.create(factory.get(), element));
    }

    public static <R extends PsiReference, T> @Nullable T resolve(R reference, ResolveCache.AbstractResolver<R, T> resolver) {
        Project project = reference.getElement().getProject();
        if (DumbService.isDumb(project)) {
            return resolver.resolve(reference, false);
        }
        return ResolveCache.getInstance(project).resolveWithCaching(reference, resolver, true, false);
    }

    public static <R extends PsiPolyVariantReference> ResolveResult[] multiResolve(
        R reference,
        ResolveCache.PolyVariantResolver<R> resolver,
        boolean incompleteCode
    ) {
        Project project = reference.getElement().getProject();
        if (DumbService.isDumb(project)) {
            return resolver.resolve(reference, incompleteCode);
        }
        return ResolveCache.getInstance(project).resolveWithCaching(reference, resolver, true, incompleteCode);
    }
}

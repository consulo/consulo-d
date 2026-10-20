package consulo.d.language.resolve;

import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

/**
 * The name a {@link PsiScopeProcessor} looks for, so scopes can skip the declarations of other names without handing them
 * to the processor ({@link DStubDeclarationWalker} does).
 */
public interface DNameHint {
    Key<DNameHint> KEY = Key.create("D_NAME_HINT");

    String getName();

    static @Nullable String getName(PsiScopeProcessor processor) {
        DNameHint hint = processor.getHint(KEY);
        return hint != null ? hint.getName() : null;
    }
}

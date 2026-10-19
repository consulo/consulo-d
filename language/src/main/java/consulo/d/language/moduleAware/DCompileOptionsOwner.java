package consulo.d.language.moduleAware;

import org.jspecify.annotations.Nullable;

/**
 * Implemented by an order entry model whose files are compiled with options of their own - a DUB package is compiled
 * with its own {@code versions}, not with those of the module depending on it.
 */
public interface DCompileOptionsOwner {
    /**
     * @return {@code null} to fall back to the options of the module owning the entry
     */
    @Nullable DCompileOptions getCompileOptions();
}

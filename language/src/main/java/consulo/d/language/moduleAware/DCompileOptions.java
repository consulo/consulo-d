package consulo.d.language.moduleAware;

import java.util.List;
import java.util.TreeSet;

/**
 * What a module is compiled with that changes which declarations exist: {@code -version=X} and {@code -debug=X}
 * identifiers, including the predefined ones a DUB import knows about (platform, architecture, compiler).
 * <p>
 * Both lists are sorted and free of duplicates, so equal options serialize to equal bytes.
 */
public record DCompileOptions(List<String> versions, List<String> debugIdentifiers) {
    public static final DCompileOptions EMPTY = new DCompileOptions(List.of(), List.of());

    public DCompileOptions {
        versions = List.copyOf(new TreeSet<>(versions));
        debugIdentifiers = List.copyOf(new TreeSet<>(debugIdentifiers));
    }

    public boolean isEmpty() {
        return versions.isEmpty() && debugIdentifiers.isEmpty();
    }

    public String format() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(", ", versions));
        if (!debugIdentifiers.isEmpty()) {
            if (!builder.isEmpty()) {
                builder.append("; ");
            }
            builder.append("debug: ").append(String.join(", ", debugIdentifiers));
        }
        return builder.toString();
    }
}

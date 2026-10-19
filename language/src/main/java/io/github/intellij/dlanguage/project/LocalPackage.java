package io.github.intellij.dlanguage.project;

/**
 * This should be used to represent a dependency in a project that is not using dub
 * @since v1.16.2
 * @author Samael Bate (singingbush)
 * created on 06/02/18
 */
public record LocalPackage(
    String name,
    String path
) implements Dependency {
    @Override
    public String getDisplayName() {
        return name;
    }
}

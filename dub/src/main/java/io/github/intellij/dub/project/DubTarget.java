package io.github.intellij.dub.project;

import io.github.intellij.dlanguage.project.BuildSettings;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record DubTarget(
    String rootPackage,
    List<String> packages,
    String rootConfiguration,
    @Nullable BuildSettings buildSettings
) {
}

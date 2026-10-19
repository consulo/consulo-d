package io.github.intellij.dub.packageConfig.inspections;

import com.intellij.json.psi.JsonElement;
import com.intellij.json.psi.JsonProperty;

import java.util.Map;

public record JsonPackageDependency(
    String name,
    JsonProperty element,
    Map<String, JsonElement> properties) {
}

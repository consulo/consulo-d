package io.github.intellij.dub.packageConfig.inspections;

import com.intellij.json.psi.JsonElement;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Top-level declarations of upstream's {@code DubPackageDependencyVisitor.kt}.
 */
public final class DubPackageDependencyVisitorKt {
    private DubPackageDependencyVisitorKt() {
    }

    public static Map<String, JsonElement> collectElements(JsonProperty packageElement) {
        JsonValue value = packageElement.getValue();
        if (!(value instanceof JsonObject properties)) {
            if (value == null) {
                return Map.of();
            }
            return Map.of("version", value);
        }
        Map<String, JsonElement> result = new LinkedHashMap<>();
        for (JsonProperty it : properties.getPropertyList()) {
            String name = it.getName();
            JsonValue propertyValue = it.getValue();
            if (propertyValue == null) {
                continue;
            }
            result.put(name, propertyValue);
        }
        return result;
    }
}

package io.github.intellij.dub.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.intellij.dub.project.DubPackage;
import io.github.intellij.dub.project.DubPackageFile;
import io.github.intellij.dub.project.DubProject;
import io.github.intellij.dub.project.DubTarget;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DescribeParserImpl implements DescribeParser {

    // some handy extension functions for Gson:
    private static String asString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null ? element.getAsString() : "";
    }

    private static List<String> asStringArray(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonElement item : element.getAsJsonArray()) {
            result.add(item.getAsString());
        }
        return result;
    }

    @Override
    public DubProject parse(Reader reader) throws DescribeParserException {
        try {
            return processJsonObject(JsonParser.parseReader(reader).getAsJsonObject());
        }
        catch (JsonSyntaxException e) {
            throw new DescribeParserException("There was a problem parsing the result of dub describe", e);
        }
    }

    @Override
    public DubProject parse(String json) throws DescribeParserException {
        try {
            return processJsonObject(JsonParser.parseString(json).getAsJsonObject());
        }
        catch (JsonSyntaxException e) {
            throw new DescribeParserException("There was a problem parsing the result of dub describe", e);
        }
    }

    private DubProject processJsonObject(JsonObject jsonData) throws DescribeParserException {
        String rootPackageName = asString(jsonData, "rootPackage");
        RootAndOthers allPackages = parsePackages(rootPackageName, jsonData.get("packages").getAsJsonArray());

        return new DubProject(
            rootPackageName,
            allPackages.root(),
            asString(jsonData, "configuration"),
            asString(jsonData, "buildType"),
            asString(jsonData, "compiler"),
            asStringArray(jsonData, "architecture"),
            asStringArray(jsonData, "platform"),
            allPackages.others(),
            parseTargets(jsonData.get("targets").getAsJsonArray())
        );
    }

    private record RootAndOthers(DubPackage root, List<DubPackage> others) {
    }

    private RootAndOthers parsePackages(String rootPackageName, JsonArray packages) throws DescribeParserException {
        DubPackage rootPackage = null;
        List<DubPackage> packageList = new ArrayList<>(packages.size());

        for (JsonElement element : packages) {
            DubPackage dubPackage = jsonToDubPackage((JsonObject) element);
            if (rootPackageName.equals(dubPackage.name())) {
                rootPackage = dubPackage;
            }
            else {
                packageList.add(dubPackage);
            }
        }

        if (rootPackage == null) {
            throw new DescribeParserException("Could not establish root package");
        }
        return new RootAndOthers(rootPackage, packageList);
    }

    private List<DubTarget> parseTargets(JsonArray jsonArray) {
        List<DubTarget> targets = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            JsonObject it = (JsonObject) element;
            targets.add(new DubTarget(
                asString(it, "rootPackage"),
                asStringArray(it, "packages"),
                asString(it, "rootConfiguration"),
                null // todo: BuildSettings() //it.asString("buildSettings")
            ));
        }
        return targets;
    }

    private DubPackage jsonToDubPackage(JsonObject json) {
        List<DubPackageFile> files = new ArrayList<>();
        @Nullable JsonArray filesArray = json.get("files").getAsJsonArray();
        if (filesArray != null) {
            for (JsonElement f : filesArray) {
                JsonObject obj = f.getAsJsonObject();
                files.add(new DubPackageFile(asString(obj, "role"), asString(obj, "path")));
            }
        }

        return new DubPackage(
            asString(json, "name"),
            asString(json, "path"),
            asString(json, "version"),
            asString(json, "targetPath"),
            asString(json, "targetFileName"),
            asString(json, "description"),
            "",
            List.of(),
            asString(json, "copyright"),
            asString(json, "license"),
            asStringArray(json, "dependencies"),
            "",
            "",
            asStringArray(json, "libs"),
            asStringArray(json, "copyFiles"),
            asStringArray(json, "extraDependencyFiles"),
            asStringArray(json, "versions"),
            asStringArray(json, "debugVersions"),
            asStringArray(json, "importPaths"), // importPaths, eg: "source",
            asStringArray(json, "stringImportPaths"), // stringImportPaths, eg: "views",
            asStringArray(json, "sourceFiles"),
            asStringArray(json, "stringImportFiles"),
            asStringArray(json, "preGenerateCommands"),
            asStringArray(json, "postGenerateCommands"),
            asStringArray(json, "preBuildCommands"),
            asStringArray(json, "postBuildCommands"),
            asStringArray(json, "preRunCommands"),
            asStringArray(json, "postRunCommands"),
            //environments = json.asStringArray("environments"),
            //buildEnvironments = json.asStringArray("buildEnvironments"),
            //runEnvironments = json.asStringArray("runEnvironments"),
            //preGenerateEnvironments = json.asStringArray("preGenerateEnvironments"),
            //postGenerateEnvironments = json.asStringArray("postGenerateEnvironments"),
            //preBuildEnvironments = json.asStringArray("preBuildEnvironments"),
            //postBuildEnvironments = json.asStringArray("postBuildEnvironments"),
            //preRunEnvironments = json.asStringArray("preRunEnvironments"),
            //postRunEnvironments = json.asStringArray("postRunEnvironments"),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            asStringArray(json, "options"),
            files
        );
    }
}

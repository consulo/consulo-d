package io.github.intellij.dub.packageConfig.inspections;

import com.intellij.json.psi.JsonElement;
import com.intellij.json.psi.JsonStringLiteral;
import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.localize.LocalizeValue;
import consulo.util.lang.SemVer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.github.intellij.dub.packageConfig.inspections.PackageVersionInspectionKt.isSemVer;
import static io.github.intellij.dub.packageConfig.inspections.PackageVersionInspectionKt.toSemVer;

public class JsonDependencyVersionVisitor extends DubJsonDependencyVisitor {

    public JsonDependencyVersionVisitor(ProblemsHolder holder) {
        super(holder);
    }

    @Override
    public void visitPackage(JsonPackageDependency packageDependency) {
        JsonElement version = packageDependency.properties().get("version");
        if (version == null) {
            return;
        }
        if (!(version instanceof JsonStringLiteral packageVersionElement)) {
            registerProblem(version, DLocalize.inspectionPackageVersionValidationNotString());
            return;
        }
        String dependencyVersion = packageVersionElement.getValue();

        if (dependencyVersion.equals("*")) {
            return;
        }
        // Check if minor simple (~>X.Y.Z)
        if ((dependencyVersion.startsWith("~>") || dependencyVersion.startsWith("=="))
            && isSemVer(dependencyVersion.substring(2))
        ) {
            return;
        }
        List<String> range = Arrays.asList(dependencyVersion.split(" ", -1));
        if (range.size() > 2) {
            registerProblem(packageVersionElement, DLocalize.inspectionPackageVersionValidationInvalid(dependencyVersion));
            return;
        }
        if (range.stream().allMatch(it -> (it.startsWith(">") || it.startsWith("<")
            || it.startsWith(">=") || it.startsWith(">=")) &&
            isSemVer(trimComparison(it))
        )) {
            if (range.size() == 2) {
                boolean lessThanOrEquals = range.get(0).startsWith("<=");
                if (range.get(0).startsWith("<=") || lessThanOrEquals) {
                    String prefix = lessThanOrEquals ? "<=" : "<";
                    registerProblem(packageVersionElement, DLocalize.inspectionPackageVersionValidationFirstOperator(prefix));
                    return;
                }
                SemVer first = Objects.requireNonNull(toSemVer(trimComparison(range.get(0))));
                SemVer second = Objects.requireNonNull(toSemVer(trimComparison(range.get(1))));
                if (first.compareTo(second) > 0) {
                    registerProblem(packageVersionElement, DLocalize.inspectionPackageVersionValidationRangeOrder());
                    return;
                }
            }
            return;
        }
        // try raw version (X.Y.Z)
        if (isSemVer(dependencyVersion)) {
            return;
        }
        // try branch version
        if (dependencyVersion.startsWith("~") && PackageVersionInspectionKt.isLatinAlphanumeric(dependencyVersion.substring(1))) {
            return;
        }
        registerProblem(packageVersionElement, DLocalize.inspectionPackageVersionValidationInvalid(dependencyVersion));
    }

    private void registerProblem(JsonElement element, LocalizeValue message) {
        getHolder().newProblem(message)
            .range(element)
            .highlightType(ProblemHighlightType.ERROR)
            .create();
    }

    /**
     * Kotlin's {@code trimStart('>', '<', '=')}.
     */
    private static String trimComparison(String text) {
        int start = 0;
        while (start < text.length() && (text.charAt(start) == '>' || text.charAt(start) == '<' || text.charAt(start) == '=')) {
            start++;
        }
        return text.substring(start);
    }
}

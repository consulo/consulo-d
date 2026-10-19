package io.github.intellij.dub.packageConfig.inspections;

import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiFile;

public abstract class DubJsonDependencyVisitor extends JsonElementVisitor {
    private final ProblemsHolder myHolder;

    public DubJsonDependencyVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitProperty(JsonProperty o) {
        if (!(o.getParent() instanceof JsonObject currentLibParentObject)) {
            return;
        }
        if (!(currentLibParentObject.getParent() instanceof JsonProperty parentBlockProperty)) {
            return;
        }
        if (!"dependencies".equals(parentBlockProperty.getName())) {
            return;
        }
        if (!(parentBlockProperty.getParent() instanceof JsonObject topLevelObject)) {
            return;
        }
        if (!(topLevelObject.getParent() instanceof PsiFile)) {
            return;
        }

        JsonPackageDependency dependency = new JsonPackageDependency(o.getName(), o, DubPackageDependencyVisitorKt.collectElements(o));
        visitPackage(dependency);
    }

    /**
     * Implement this method to visit a dependency in dub.json
     */
    public abstract void visitPackage(JsonPackageDependency packageDependency);
}

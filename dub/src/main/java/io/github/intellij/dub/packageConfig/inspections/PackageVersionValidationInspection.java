package io.github.intellij.dub.packageConfig.inspections;

import com.intellij.json.JsonLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;

@ExtensionImpl
public class PackageVersionValidationInspection extends LocalInspectionTool {
    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        if (!holder.getFile().getName().equals("dub.json")) {
            return super.buildVisitor(holder, isOnTheFly);
        }
        return new JsonDependencyVersionVisitor(holder);
    }

    @Override
    public Language getLanguage() {
        return JsonLanguage.INSTANCE;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.inspectionPackageVersionValidationName();
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return DLocalize.inspectionGroupDubPackages();
    }

    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{DLocalize.inspectionGroupDLanguage(), getGroupDisplayName()};
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
    public LocalizeValue getDescription() {
        return DLocalize.inspectionPackageVersionValidationDescription();
    }
}

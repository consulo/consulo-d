package io.github.intellij.dlanguage.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.DLanguage;

@ExtensionImpl
public class AutoFunctionWithoutReturn extends LocalInspectionTool {
    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dInspectionsDescriptionAutofunctionwithoutreturn();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dInspectionsSymbolAutofunctionwithoutreturnDisplayname();
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return DLocalize.inspectionGroupDLanguage();
    }

    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{getGroupDisplayName()};
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new AutoFunctionReturnVisitor(holder);
    }
}

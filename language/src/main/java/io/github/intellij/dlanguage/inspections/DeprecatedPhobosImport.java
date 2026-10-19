package io.github.intellij.dlanguage.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangVisitor;

@ExtensionImpl
public class DeprecatedPhobosImport extends LocalInspectionTool {
    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dInspectionsDescriptionDeprecatedphobosimport();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dInspectionsSymbolDeprecatedphobosimportDisplayname();
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return DLocalize.dInspectionsGroupDeprecatedSyntax();
    }

    @Override
    public LocalizeValue[] getGroupPath() {
        return new LocalizeValue[]{DLocalize.inspectionGroupDLanguage(), getGroupDisplayName()};
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
    public DlangVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new DeprecatedPhobosImportVisitor(holder);
    }
}

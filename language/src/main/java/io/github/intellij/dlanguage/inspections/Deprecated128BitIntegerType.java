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

// https://dlang.org/deprecate.html#128-bit%20integer%20types
@ExtensionImpl
public class Deprecated128BitIntegerType extends LocalInspectionTool {
    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dInspectionsDescriptionDeprecated128bitintegertype();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dInspectionsSymbolDeprecated128bitintegertypeDisplayname();
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
    public DlangVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new Deprecated128BitIntegerTypeVisitor(holder);
    }
}

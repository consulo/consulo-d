package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DlangVisitor;

/**
 * todo 100_000L is incorrectly flagged as wrong
 */
public class FormatNumbersForReadability extends LocalInspectionTool {
    @Override
    public DlangVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new FormatNumbersForReadabilityVisitor(holder);
    }

    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dInspectionsDescriptionNumbersreadability();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dInspectionsSymbolFormatnumbersforreadabilityDisplayname();
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
}

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
 * Created by francis on 1/5/2018.
 */
public class EnumArrayLiteralsInClassDeclaration extends LocalInspectionTool {
    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dInspectionsDescriptionEnumarrayliteralsinclassdeclaration();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return DLocalize.dInspectionsSymbolEnumarrayliteralsinclassdeclarationDisplayname();
    }

    @Override
    public DlangVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new EnumArrayLiteralsInClassDeclarationVisitor(holder);
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

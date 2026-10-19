package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.types.DPrimitiveType;
import io.github.intellij.dlanguage.psi.types.DType;

public class Deprecated128BitIntegerTypeVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public Deprecated128BitIntegerTypeVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitBasicType(DLanguageBasicType type) {
        DType dType = type.getDType();
        if (DPrimitiveType.CENT.equals(dType) || DPrimitiveType.UCENT.equals(dType)) {
            myHolder.newProblem(DLocalize.dInspectionsSymbolDeprecated128bitintegertypeDescription())
                .range(type)
                .withFixes(
                    new UseAlternativeInt128(type, "std.int128"),
                    new UseAlternativeInt128(type, "core.int128")
                )
                .create();
        }
    }
}

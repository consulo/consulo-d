package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageLastCatch;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageCatch;

public class CatchingGeneralExceptionsVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public CatchingGeneralExceptionsVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitCatch(DLanguageCatch o) {
        String exceptionType = null;
        DLanguageType type = o.getType();
        DLanguageBasicType basicType = type != null ? type.getBasicType() : null;
        DLanguageQualifiedIdentifier qualifiedIdentifier = basicType != null ? basicType.getQualifiedIdentifier() : null;
        if (qualifiedIdentifier != null) {
            exceptionType = qualifiedIdentifier.getText();
        }
        if (exceptionType != null) {
            if (exceptionType.equals("Error") || exceptionType.equals("Throwable")) {
                myHolder.newProblem(DLocalize.dInspectionsSymbolCatchinggeneralexceptionsMessage())
                    .range(o)
                    .create();
            }
        }
    }

    @Override
    public void visitLastCatch(DLanguageLastCatch o) {
        myHolder.newProblem(DLocalize.dInspectionsSymbolCatchinggeneralexceptionsMessage())
            .range(o)
            .create();
    }

}

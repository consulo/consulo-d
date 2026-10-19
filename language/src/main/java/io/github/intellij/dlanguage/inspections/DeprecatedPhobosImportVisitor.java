package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.localize.LocalizeValue;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageSingleImport;

public class DeprecatedPhobosImportVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public DeprecatedPhobosImportVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitImportDeclaration(DLanguageImportDeclaration declaration) {
        for (DLanguageSingleImport it : declaration.getSingleImports()) {
            switch (it.getText()) {
                case "std.experimental.checkedint" -> register(it, DLocalize.dInspectionsSymbolDeprecatedphobosimportMessageCheckedint());
                case "std.experimental.logger" -> register(it, DLocalize.dInspectionsSymbolDeprecatedphobosimportMessageLogger());
                case "std.xml" -> register(it, DLocalize.dInspectionsSymbolDeprecatedphobosimportMessageXml());
                default -> {
                }
            }
        }
    }

    public void register(DLanguageSingleImport dlangSingleImport, LocalizeValue descriptionTemplate) {
        myHolder.newProblem(descriptionTemplate)
            .range(dlangSingleImport)
            .highlightType(ProblemHighlightType.LIKE_DEPRECATED)
            .create();
    }
}

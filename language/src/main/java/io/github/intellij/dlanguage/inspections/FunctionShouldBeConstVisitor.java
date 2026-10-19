package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.quickfix.MakeFunctionConst;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;

import java.util.Objects;

public class FunctionShouldBeConstVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public FunctionShouldBeConstVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitFunctionDeclaration(DLanguageFunctionDeclaration o) {
        String name = o.getName();
        if (Objects.equals(name, "opCmp") || Objects.equals(name, "opEquals") || Objects.equals(name, "toHash")) {
            DAttributesFinder finder = new DAttributesFinder(o);//todo make function declarations have an isConst method.
            finder.recurseUp();
            if (!finder.isConst()) {
                myHolder.newProblem(DLocalize.dInspectionsSymbolFunctionshouldbeconstMessage())
                    .range(o)
                    .withFixes(new MakeFunctionConst(o))
                    .create();
            }
        }
    }
}

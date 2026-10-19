package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;

import java.util.Set;

public class BuiltinPropertyNameVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;
    private final Set<String> myProperties = Set.of("init", "sizeof", "mangleof", "alignof", "stringof");

    public BuiltinPropertyNameVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    public Set<String> getProperties() {
        return myProperties;
    }

    @Override
    public void visitDNamedElement(DNamedElement o) {
        String name = o.getName();
        if (name != null && myProperties.contains(name)) {
            myHolder.newProblem(DLocalize.dInspectionsSymbolBuiltinpropertynameMessage(name))
                .range(o)
                .create();
        }
    }
}

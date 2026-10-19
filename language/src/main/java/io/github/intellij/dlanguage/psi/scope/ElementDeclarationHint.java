package io.github.intellij.dlanguage.psi.scope;

import consulo.util.dataholder.Key;

public interface ElementDeclarationHint {
    Key<ElementDeclarationHint> KEY = Key.create("ElementDeclarationHint");

    enum DeclarationKind {
        IMPORT,
        CLASS,
        INTERFACE,

        // add more if necessary
    }

    boolean shouldProcess(DeclarationKind kind);
}

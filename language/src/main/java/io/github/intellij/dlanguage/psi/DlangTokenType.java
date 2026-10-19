package io.github.intellij.dlanguage.psi;

import consulo.language.ast.IElementType;
import io.github.intellij.dlanguage.DLanguage;
import org.jetbrains.annotations.NonNls;

public class DlangTokenType extends IElementType {
    private final String myName;

    public DlangTokenType(@NonNls String name) {
        super(name, DLanguage.INSTANCE);
        myName = name;
    }

    public String getName() {
        return myName;
    }

    @Override
    public String toString() {
        return "DlangTokenType." + super.toString();
    }
}

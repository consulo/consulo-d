package io.github.intellij.dlanguage.psi;

import consulo.language.ast.IElementType;
import io.github.intellij.dlanguage.DLanguage;
import org.jetbrains.annotations.NonNls;

public class DlangElementType extends IElementType {
    public DlangElementType(@NonNls String debugName) {
        super(debugName, DLanguage.INSTANCE);
    }
}

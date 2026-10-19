package io.github.intellij.dlanguage.documentation.psi;

import consulo.language.ast.IElementType;
import io.github.intellij.dlanguage.documentation.DDocLanguage;

public class DlangDocElementType extends IElementType {
    public DlangDocElementType(String debugName) {
        super(debugName, DDocLanguage.INSTANCE);
    }
}

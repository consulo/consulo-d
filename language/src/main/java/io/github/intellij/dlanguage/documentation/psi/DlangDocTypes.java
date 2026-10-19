package io.github.intellij.dlanguage.documentation.psi;

import consulo.language.ast.IElementType;
import io.github.intellij.dlanguage.documentation.DDocLanguage;

public final class DlangDocTypes {
    public static final DlangDocTypes INSTANCE = new DlangDocTypes();

    private final IElementType myDocDataType = new IElementType("DOC_DATA", DDocLanguage.INSTANCE);

    private DlangDocTypes() {
    }

    public IElementType getDocDataType() {
        return myDocDataType;
    }
}

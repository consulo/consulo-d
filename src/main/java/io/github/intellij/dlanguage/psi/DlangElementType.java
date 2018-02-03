package io.github.intellij.dlanguage.psi;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.psi.tree.IElementType;
import consulo.d.DLanguage;

public class DlangElementType extends IElementType {

    public DlangElementType(@NotNull @NonNls final String debugName) {
        super(debugName, DLanguage.INSTANCE);
    }
}

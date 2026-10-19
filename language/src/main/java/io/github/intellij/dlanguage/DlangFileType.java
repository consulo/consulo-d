package io.github.intellij.dlanguage;

import consulo.d.localize.DLocalize;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

/**
 * Port of upstream {@code DlangFileType.kt}. Upstream's {@code OSFileIdeAssociation} has no Consulo counterpart.
 */
public final class DlangFileType extends LanguageFileType {
    public static final DlangFileType INSTANCE = new DlangFileType();

    public static final String DEFAULT_EXTENSION = "d";

    private DlangFileType() {
        super(DLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "D file";
    }

    @Override
    public LocalizeValue getDescription() {
        return DLocalize.dlangFiletype();
    }

    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Image getIcon() {
        return DLanguage.Icons.FILE;
    }
}

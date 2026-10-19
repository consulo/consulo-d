package io.github.intellij.dlanguage.documentation;

import consulo.language.DependentLanguage;
import consulo.language.Language;
import io.github.intellij.dlanguage.DLanguage;

public final class DDocLanguage extends Language implements DependentLanguage {
    public static final DDocLanguage INSTANCE = new DDocLanguage();

    private DDocLanguage() {
        super(DLanguage.INSTANCE, "DDoc");
    }
}

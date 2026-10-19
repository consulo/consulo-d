package io.github.intellij.dlanguage.template;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.editor.template.context.BaseTemplateContextType;
import consulo.language.editor.template.context.TemplateActionContext;
import consulo.language.psi.PsiUtilCore;
import io.github.intellij.dlanguage.DLanguage;
import jakarta.inject.Inject;

@ExtensionImpl
public class DlangContext extends BaseTemplateContextType {
    @Inject
    protected DlangContext() {
        super("D", DLocalize.liveTemplateContextD());
    }

    @Override
    public boolean isInContext(TemplateActionContext context) {
        return PsiUtilCore.getLanguageAtOffset(context.getFile(), context.getStartOffset()).isKindOf(DLanguage.INSTANCE);
    }
}

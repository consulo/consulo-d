package io.github.intellij.dlanguage.codeinsight;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.parameterInfo.CreateParameterInfoContext;
import consulo.language.editor.parameterInfo.ParameterInfoContext;
import consulo.language.editor.parameterInfo.ParameterInfoHandler;
import consulo.language.editor.parameterInfo.ParameterInfoUIContext;
import consulo.language.editor.parameterInfo.UpdateParameterInfoContext;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import org.jspecify.annotations.Nullable;

import static consulo.language.psi.util.PsiTreeUtil.getParentOfType;
import static io.github.intellij.dlanguage.codeinsight.ParameterInfoKt.removeParentheses;

@ExtensionImpl
public class TemplateParameterInfo implements ParameterInfoHandler<DLanguageTemplateInstance, DLanguageTemplateParameters> {
    @Override
    public void updateParameterInfo(DLanguageTemplateInstance parameterOwner, UpdateParameterInfoContext context) {

    }

    @Override
    public void showParameterInfo(DLanguageTemplateInstance templateExpression, CreateParameterInfoContext context) {
        PsiReference reference = templateExpression.getReference();
        if (reference == null) {
            return;
        }
        PsiElement resolved = reference.resolve();
        if (!(resolved instanceof DLanguageTemplateDeclaration templateDeclaration)) {
            return;
        }
        context.setItemsToShow(new Object[]{templateDeclaration.getTemplateParameters()});

        context.showHint(templateExpression, 0, this);
    }

    @Override
    public void updateUI(DLanguageTemplateParameters p, ParameterInfoUIContext context) {
        context.signature().text(removeParentheses(p.getText())).apply();
    }

    @Override
    public @Nullable DLanguageTemplateInstance findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
        return findTemplateInstanceFromCursor(context);
    }

    @Override
    public @Nullable DLanguageTemplateInstance findElementForParameterInfo(CreateParameterInfoContext context) {
        return findTemplateInstanceFromCursor(context);
    }

    private @Nullable DLanguageTemplateInstance findTemplateInstanceFromCursor(ParameterInfoContext context) {
        int listStart = context.getOffset();
        PsiFile file = context.getFile();
        PsiElement element = file.findElementAt(listStart);
        DLanguageTemplateInstance newExpression = getParentOfType(element, DLanguageTemplateInstance.class);
        return newExpression;
    }

    // Consulo declares these without IntelliJ's defaults, which they repeat
    @Override
    public boolean couldShowInLookup() {
        return false;
    }

    @Override
    public Object @Nullable [] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return null;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

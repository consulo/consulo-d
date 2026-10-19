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
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageNewExpression;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static consulo.language.psi.util.PsiTreeUtil.findChildrenOfType;
import static consulo.language.psi.util.PsiTreeUtil.getParentOfType;
import static io.github.intellij.dlanguage.codeinsight.ParameterInfoKt.removeParentheses;

@ExtensionImpl
public class ConstructorParameterInfo implements ParameterInfoHandler<DLanguageNewExpression, DLanguageParameters> {
    @Override
    public void updateParameterInfo(DLanguageNewExpression parameterOwner, UpdateParameterInfoContext context) {

    }

    @Override
    public void showParameterInfo(DLanguageNewExpression newExpression, CreateParameterInfoContext context) {
        DLanguageType type = newExpression.getType();
        DLanguageBasicType basicType = type == null ? null : type.getBasicType();
        DLanguageQualifiedIdentifier qualifiedIdentifier = basicType == null ? null : basicType.getQualifiedIdentifier();
        PsiReference reference = qualifiedIdentifier == null ? null : qualifiedIdentifier.getReference();
        if (reference == null) {
            return;
        }
        PsiElement resolved = reference.resolve();
        if (!(resolved instanceof DLanguageClassDeclaration classDeclaration)) {
            return;
        }
        DLanguageStructBody structBody = classDeclaration.getStructBody();
        List<DLanguageConstructor> constructors = new ArrayList<>();
        if (structBody != null) {
            for (Declaration declaration : structBody.getDeclarations()) {
                if (declaration instanceof DLanguageConstructor constructor) {
                    constructors.add(constructor);
                }
            }
        }
        List<DLanguageParameters> itemsToShow = new ArrayList<>();
        for (DLanguageConstructor constructor : constructors) {
            for (DLanguageParameters parameters : findChildrenOfType(constructor, DLanguageParameters.class)) {
                if (parameters != null) {
                    itemsToShow.add(parameters);
                }
            }
        }
        context.setItemsToShow(itemsToShow.toArray());
        context.showHint(newExpression, 0, this);
    }

    @Override
    public void updateUI(DLanguageParameters p, ParameterInfoUIContext context) {
        context.signature().text(removeParentheses(p.getText())).apply();
    }

    @Override
    public @Nullable DLanguageNewExpression findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
        return findNewExpressionFromCursor(context);
    }

    @Override
    public @Nullable DLanguageNewExpression findElementForParameterInfo(CreateParameterInfoContext context) {
        return findNewExpressionFromCursor(context);
    }

    private @Nullable DLanguageNewExpression findNewExpressionFromCursor(ParameterInfoContext context) {
        int listStart = context.getOffset();
        PsiFile file = context.getFile();
        PsiElement element = file.findElementAt(listStart);
        DLanguageNewExpression newExpression = getParentOfType(element, DLanguageNewExpression.class);
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

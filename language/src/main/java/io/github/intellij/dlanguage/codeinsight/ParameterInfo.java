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
import consulo.util.lang.Pair;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageFunctionCallExpression;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import org.jspecify.annotations.Nullable;

import static consulo.language.psi.util.PsiTreeUtil.getParentOfType;

@ExtensionImpl
public class ParameterInfo implements ParameterInfoHandler<DLanguageFunctionCallExpression, Pair<@Nullable DLanguageTemplateParameters, DLanguageParameters>> {
    @Override
    public void updateParameterInfo(DLanguageFunctionCallExpression parameterOwner, UpdateParameterInfoContext context) {

    }

    @Override
    public void showParameterInfo(DLanguageFunctionCallExpression functionCallExpression, CreateParameterInfoContext context) {
        /*var reference = functionCallExpression.unaryExpression?.prmaryExpression?.identifierOrTemplateInstance?.identifier?.reference
        if (reference == null) {
            reference = functionCallExpression.unaryExpression?.primaryExpression?.identifierOrTemplateInstance?.templateInstance?.identifier?.reference
        }
        if (reference == null || reference !is DReference) {
            return
        }
        val definitionNodes = DResolveUtil.getInstance(functionCallExpression.project).findDefinitionNode(reference.element, false)
        // The following code may not clear.
        // Struct construction can be syntactically identical to function calling.
        // In the special case of struct construction we want too find parameters
        // from struct constructors.
        context.itemsToShow = definitionNodes.flatMap { functionOrStructDefinition: PsiNamedElement ->
            if (functionOrStructDefinition is StructDeclaration) {
                val structConstructors = functionOrStructDefinition.structBody?.declarations.orEmpty().filterIsInstance<Constructor>()
                structConstructors.map { Pair(it.templateParameters, it.parameters) }
            } else if (functionOrStructDefinition is FunctionDeclaration) {
                val funcDecl = functionOrStructDefinition
                listOf(Pair(funcDecl.templateParameters, funcDecl.parameters))
            } else
                emptyList()
        }.toTypedArray()
        context.showHint(functionCallExpression, 0, this)*/
    }

    @Override
    public void updateUI(Pair<@Nullable DLanguageTemplateParameters, DLanguageParameters> p, ParameterInfoUIContext context) {
        DLanguageTemplateParameters first = p.getFirst();
        context.signature().text((first == null ? "" : first.getText() + " ! ") + p.getSecond().getText()).apply();
    }

    @Override
    public @Nullable DLanguageFunctionCallExpression findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
        int listStart = context.getOffset();
        PsiFile file = context.getFile();
        PsiElement element = file.findElementAt(listStart);
        DLanguageFunctionCallExpression functionCallExpression = getParentOfType(element, DLanguageFunctionCallExpression.class);
        return functionCallExpression;
    }

    @Override
    public @Nullable DLanguageFunctionCallExpression findElementForParameterInfo(CreateParameterInfoContext context) {
        int listStart = context.getOffset();
        PsiFile file = context.getFile();
        PsiElement element = file.findElementAt(listStart);
        DLanguageFunctionCallExpression functionCallExpression = getParentOfType(element, DLanguageFunctionCallExpression.class);
        return functionCallExpression;
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

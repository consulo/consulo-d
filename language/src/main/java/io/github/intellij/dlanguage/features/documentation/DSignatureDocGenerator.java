package io.github.intellij.dlanguage.features.documentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.component.ProcessCanceledException;
import consulo.d.language.DDocHtmlUtil;
import consulo.language.ast.IElementType;
import consulo.language.editor.documentation.DocumentationMarkup;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import io.github.intellij.dlanguage.colors.DColor;
import io.github.intellij.dlanguage.psi.DLanguageAutoDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedVariableDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageTemplateAliasParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateArgument;
import io.github.intellij.dlanguage.psi.DLanguageTemplateArguments;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageTemplateParameters;
import io.github.intellij.dlanguage.psi.DLanguageTemplateSingleArgument;
import io.github.intellij.dlanguage.psi.DLanguageTemplateThisParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTupleParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTypeParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateValueParameter;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DLanguageTypeSuffix;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.interfaces.TemplateParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DSignatureDocGenerator {

    // Consulo has no highlighting saturation setting: DDocHtmlUtil.appendStyledSpan takes no saturation factor

    public void appendDeclarationHeader(StringBuilder builder, PsiElement element, PsiElement originalElement) {
        String content = getDeclarationContent(element, originalElement);
        if (content.isBlank()) {
            return;
        }
        builder.append(DocumentationMarkup.DEFINITION_START);
        builder.append(content);
        builder.append(DocumentationMarkup.DEFINITION_END);
    }

    @RequiredReadAction
    public String getDeclarationContent(PsiElement element, PsiElement originalElement) {
        StringBuilder builder = new StringBuilder();
        // TODO number coloration in simple assign expressions
        // TODO handle final class, final members (fix attributes finder to support final)
        // TODO handle __gshared (fix attributes handling in general)
        if (element instanceof DLanguageFunctionDeclaration functionDeclaration) {
            appendFunctionSignature(builder, functionDeclaration, originalElement);
        }
        else if (element instanceof DLanguageEnumDeclaration enumDeclaration) {
            appendEnumSignature(builder, enumDeclaration);
        }
        else if (element instanceof DLanguageStructDeclaration structDeclaration) {
            appendStructSignature(builder, structDeclaration);
        }
        else if (element instanceof DLanguageUnionDeclaration unionDeclaration) {
            appendUnionSignature(builder, unionDeclaration);
        }
        else if (element instanceof DLanguageTemplateMixinDeclaration templateMixinDeclaration) {
            appendTemplateMixinDeclaration(builder, templateMixinDeclaration);
        }
        else if (element instanceof DLanguageTemplateDeclaration templateDeclaration) {
            appendTemplateDeclaration(builder, templateDeclaration);
        }
        else if (element instanceof DLanguageInterfaceDeclaration interfaceDeclaration) {
            appendInterfaceSignature(builder, interfaceDeclaration);
        }
        else if (element instanceof DLanguageClassDeclaration classDeclaration) {
            appendClassSignature(builder, classDeclaration);
        }
        else if (element instanceof DLanguageParameter parameter) {
            appendParameterSignature(builder, parameter);
        }
        else if (element instanceof DLanguageIdentifierInitializer identifierInitializer) {
            DLanguageSpecifiedVariableDeclaration parent = (DLanguageSpecifiedVariableDeclaration) identifierInitializer.getParent();
            appendType(builder, Objects.requireNonNull(parent.getBasicType()), parent.getTypeSuffixs());
            builder.append(" ").append(textOf(identifierInitializer.getIdentifier()));
        }
        else if (element instanceof DLanguageAutoAssignment autoAssignment) {
            appendAutoAssignmentSignature(builder, autoAssignment);
        }
        else if (element instanceof DLanguageAliasInitializer aliasInitializer) {
            appendAliasInitializerSignature(builder, aliasInitializer);
        }
        else if (element instanceof TemplateParameter templateParameter) {
            appendTemplateParameter(builder, templateParameter);
        }
        else if (element instanceof DLanguageEnumMember enumMember) {
            builder.append(Objects.requireNonNull(enumMember.getIdentifier()).getText());
            if (enumMember.getExpression() != null) {
                builder.append(" = ").append(Objects.requireNonNull(enumMember.getExpression()).getText());
            }
        }
        return builder.toString();
    }

    private @Nullable DAttributesFinder getAttributesForDeclaration(PsiElement element) {
        DAttributesFinder attributes = null;
        try {
            DAttributesFinder attributeFinder;
            PsiReference reference = element.getReference();
            if (reference != null) {
                PsiElement resolveResult = reference.resolve();
                if (resolveResult == null) {
                    return null;
                }
                attributeFinder = new DAttributesFinder(resolveResult);
            }
            else {
                attributeFinder = new DAttributesFinder(element);
            }
            attributeFinder.recurseUp();
            attributes = attributeFinder;
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (Exception ignored) {
        }
        return attributes;
    }

    @RequiredReadAction
    private void appendFunctionSignature(StringBuilder builder, DLanguageFunctionDeclaration element, PsiElement originalElement) {
        DAttributesFinder attributes = getAttributesForDeclaration(originalElement);

        if (attributes != null) {
            builder.append(getPrefixAttributes(attributes));
        }
        if (element.getBasicType() != null) {
            appendType(builder, element.getBasicType(), element.getTypeSuffixes());
        }
        else if (element.getAutoElem() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), element.getAutoElem().getText());
        }
        builder.append(" ");
        DDocHtmlUtil.appendStyledSpan(builder, DColor.FUNCTION_DEFINITION.getTextAttributesKey(), textOf(element.getIdentifier()));
        if (element.getTemplateParameters() != null) {
            appendTemplateParameters(builder, element.getTemplateParameters());
        }
        builder.append("(");
        appendParametersSignature(builder, element.getParameters());
        builder.append(")");

        if (attributes != null) {
            builder.append(getPostfixAttributes(attributes));
        }
    }


    @RequiredReadAction
    private void appendEnumSignature(StringBuilder builder, DLanguageEnumDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "enum");
        builder.append(" ").append(textOf(element.getIdentifier()));
        if (element.getType() != null) {
            builder.append(" : ");
            appendType(builder, element.getType());
        }
    }

    @RequiredReadAction
    private void appendStructSignature(StringBuilder builder, DLanguageStructDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "struct");
        builder.append(" ").append(textOf(element.getIdentifier()));
        if (element.getTemplateParameters() != null) {
            appendTemplateParameters(builder, element.getTemplateParameters());
        }
    }

    @RequiredReadAction
    private void appendUnionSignature(StringBuilder builder, DLanguageUnionDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "union");
        builder.append(" ").append(textOf(element.getIdentifier()));
    }

    @RequiredReadAction
    private void appendClassSignature(StringBuilder builder, DLanguageClassDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "class");
        builder.append(" ");
        builder.append(textOf(element.getIdentifier()));
        builder.append(" ");
        if (element.getTemplateParameters() != null) {
            appendTemplateParameters(builder, element.getTemplateParameters());
        }
        if (element.getBaseClassList() != null && !element.getBaseClassList().getBasicTypes().isEmpty()) {
            builder.append(": ");
            builder.append(element.getBaseClassList().getBasicTypes().stream().map(PsiElement::getText).collect(Collectors.joining(", ")));
        }
    }

    @RequiredReadAction
    private void appendInterfaceSignature(StringBuilder builder, DLanguageInterfaceDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "interface");
        builder.append(" ");
        builder.append(textOf(element.getIdentifier()));
        builder.append(" ");
        if (element.getTemplateParameters() != null) {
            appendTemplateParameters(builder, element.getTemplateParameters());
        }
        if (element.getBaseClassList() != null && !element.getBaseClassList().getBasicTypes().isEmpty()) {
            builder.append(": ");
            builder.append(element.getBaseClassList().getBasicTypes().stream().map(PsiElement::getText).collect(Collectors.joining(", ")));
        }
    }

    @RequiredReadAction
    private void appendTemplateMixinDeclaration(StringBuilder builder, DLanguageTemplateMixinDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "mixin template");
        builder.append(" ");
        DDocHtmlUtil.appendStyledSpan(builder, DColor.FUNCTION_CALL.getTextAttributesKey(), Objects.requireNonNull(element.getIdentifier()).getText());
        appendTemplateParameters(builder, Objects.requireNonNull(element.getTemplateParameters()));
    }

    @RequiredReadAction
    private void appendTemplateDeclaration(StringBuilder builder, DLanguageTemplateDeclaration element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "template");
        builder.append(" ");
        DDocHtmlUtil.appendStyledSpan(builder, DColor.FUNCTION_CALL.getTextAttributesKey(), Objects.requireNonNull(element.getIdentifier()).getText());
        appendTemplateParameters(builder, Objects.requireNonNull(element.getTemplateParameters()));
        // add constraint?
    }

    @RequiredReadAction
    private void appendAutoAssignmentSignature(StringBuilder builder, DLanguageAutoAssignment element) {
        builder.append(((DLanguageAutoDeclaration) element.getParent()).getStorageClasss().stream()
            .map(it -> DDocHtmlUtil.appendStyledSpan(new StringBuilder(), DColor.KEYWORD.getTextAttributesKey(), it.getText()).toString())
            .collect(Collectors.joining(" "))).append(" ").append(textOf(element.getIdentifier()));
    }

    @RequiredReadAction
    private void appendAliasInitializerSignature(StringBuilder builder, DLanguageAliasInitializer element) {
        DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "alias");
        builder.append(" ");
        builder.append(textOf(element.getIdentifier()));
        if (element.getOP_EQ() != null) {
            builder.append(" = ");
            appendType(builder, Objects.requireNonNull(element.getType()));
        }
    }

    @RequiredReadAction
    private void appendParametersSignature(StringBuilder builder, @Nullable DLanguageParameters element) {
        if (element == null) {
            return;
        }
        builder.append(element.getParameters().stream().map(it -> {
            StringBuilder paramBuilder = new StringBuilder();
            appendParameterSignature(paramBuilder, it);
            return paramBuilder.toString();
        }).collect(Collectors.joining(", ")));
        if (element.getOP_TRIPLEDOT() != null) {
            builder.append(", ...");
        }
    }

    @RequiredReadAction
    private void appendParameterSignature(StringBuilder builder, DLanguageParameter element) {
        // Important: we need to take care of order for some keywords, order has meaning (return ref, ref return, ...)
        PsiElement current = element.getFirstChild();
        boolean isValid = true;
        while (isValid) {
            if (!(current instanceof PsiWhiteSpace)) {
                isValid = appendParameterKeyword(builder, PsiUtilCore.getElementType(current));
            }
            current = current.getNextSibling();
        }
        if (!element.getParameterAttributes().isEmpty()) {
            builder.append(element.getParameterAttributes().stream().map(PsiElement::getText).collect(Collectors.joining(" ")));
            builder.append(" ");
        }
        appendType(builder, element.getType());
        if (element.getIdentifier() != null) {
            builder.append(" ").append(element.getIdentifier().getText());
        }
        if (element.getAssignExpression() != null) {
            builder.append(" = ");
            builder.append(element.getAssignExpression().getText());
        }
        if (element.getOP_TRIPLEDOT() != null) {
            builder.append("...");
        }
    }

    private boolean appendParameterKeyword(StringBuilder builder, @Nullable IElementType element) {
        if (element == DlangTypes.KW_SCOPE) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "scope");
        }
        else if (element == DlangTypes.KW_CONST) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "const");
        }
        else if (element == DlangTypes.KW_INOUT) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "inout");
        }
        else if (element == DlangTypes.KW_SHARED) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "shared");
        }
        else if (element == DlangTypes.KW_FINAL) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "final");
        }
        else if (element == DlangTypes.KW_IN) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "in");
        }
        else if (element == DlangTypes.KW_LAZY) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "lazy");
        }
        else if (element == DlangTypes.KW_OUT) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "out");
        }
        else if (element == DlangTypes.KW_REF) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "ref");
        }
        else if (element == DlangTypes.KW_AUTO) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "auto");
        }
        else if (element == DlangTypes.KW_RETURN) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "return");
        }
        else {
            return false;
        }
        builder.append(" ");
        return true;
    }

    @RequiredReadAction
    private void appendTemplateParameters(StringBuilder builder, DLanguageTemplateParameters element) {
        builder.append("(");
        if (element.getTemplateParameterList() != null) {
            builder.append(element.getTemplateParameterList().getTemplateParameters().stream().map(it -> {
                StringBuilder templateBuilder = new StringBuilder();
                appendTemplateParameter(templateBuilder, it);
                return templateBuilder.toString();
            }).collect(Collectors.joining(", ")));
        }
        builder.append(")");
    }

    @RequiredReadAction
    private void appendTemplateParameter(StringBuilder builder, TemplateParameter element) {
        if (element instanceof DLanguageTemplateAliasParameter aliasParameter) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), Objects.requireNonNull(aliasParameter.getKW_ALIAS()).getText());
            builder.append(" ").append(Objects.requireNonNull(aliasParameter.getIdentifier()).getText());
        }
        else if (element instanceof DLanguageTemplateTupleParameter tupleParameter) {
            builder.append(Objects.requireNonNull(tupleParameter.getIdentifier()).getText()).append("...");
        }
        else if (element instanceof DLanguageTemplateTypeParameter typeParameter) {
            appendTemplateTypeParameter(builder, typeParameter);
        }
        else if (element instanceof DLanguageTemplateThisParameter thisParameter) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), Objects.requireNonNull(thisParameter.getKW_THIS()).getText());
            builder.append(" ");
            appendTemplateTypeParameter(builder, Objects.requireNonNull(thisParameter.getTemplateTypeParameter()));
        }
        else if (element instanceof DLanguageTemplateValueParameter valueParameter) {
            appendType(builder, valueParameter.getType());
            builder.append(" ").append(Objects.requireNonNull(valueParameter.getIdentifier()).getText());
            if (valueParameter.getTemplateValueParameterDefault() != null) {
                // TODO properly handle expression (add coloration of literals, ...)
                builder.append(" = ").append(Objects.requireNonNull(valueParameter.getTemplateValueParameterDefault().getExpression()).getText());
            }
        }
    }

    @RequiredReadAction
    private void appendTemplateTypeParameter(StringBuilder builder, DLanguageTemplateTypeParameter element) {
        builder.append(Objects.requireNonNull(element.getIdentifier()).getText());
        if (element.getOP_COLON() != null) {
            builder.append(" : ");
            appendType(builder, element.getTypes().get(0));
        }
        if (element.getOP_EQ() != null) {
            builder.append(" = ");
            appendType(builder, element.getTypes().get(element.getTypes().size() - 1));
        }
    }

    private String getPrefixAttributes(DAttributesFinder attributes) {
        StringBuilder builder = new StringBuilder();
        if (attributes.isProperty()) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.AT_ATTRIBUTE.getTextAttributesKey(), "@property");
            builder.append("\n");
        }
        String accessibility = null;
        if (!attributes.isLocal()) {
            if (attributes.isPrivate()) {
                accessibility = "private";
            }
            else if (attributes.isPublic()) {
                accessibility = "public";
            }
            else if (attributes.isProtected()) {
                accessibility = "protected";
            }
        }
        if (attributes.isExtern()) {
            // FIXME can be extern (C), extern (C++), extern (D), currently it’s impossible to guess
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "extern");
            if (accessibility != null) {
                builder.append(" ");
            }
        }
        if (accessibility != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), accessibility);
            builder.append(" ");
        }

        return builder.toString();
    }

    private String getPostfixAttributes(DAttributesFinder attributes) {
        StringBuilder builder = new StringBuilder();
        if (attributes.isConst()) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "const");
            builder.append(" ");
        }
        if (attributes.isImmutable()) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "immutable");
            builder.append(" ");
        }
        if (attributes.isPure()) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "pure");
            builder.append(" ");
        }
        if (attributes.isNothrow()) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "nothrow");
            builder.append(" ");
        }
        if (attributes.isNoGC()) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.AT_ATTRIBUTE.getTextAttributesKey(), "nogc");
            builder.append(" ");
        }
        // TODO enable when safety will be supported by attributes finder
        //HtmlSyntaxInfoUtil.appendStyledSpan(builder, DColor.AT_ATTRIBUTE.textAttributesKey, "@" + attributes.getSafety(), highlightingSaturation)

        return builder.toString().strip();
    }

    @RequiredReadAction
    private StringBuilder appendType(StringBuilder builder, DLanguageBasicType basicType, List<DLanguageTypeSuffix> typeSuffixes) {
        // Prefix keywords
        if (basicType.getKW_CONST() != null) {
            DDocHtmlUtil.appendStyledSpan(
                builder,
                DColor.KEYWORD.getTextAttributesKey(),
                "const"
            );
        }
        if (basicType.getKW_INOUT() != null) {
            DDocHtmlUtil.appendStyledSpan(
                builder,
                DColor.KEYWORD.getTextAttributesKey(),
                "inout"
            );
        }
        if (basicType.getOP_PAR_LEFT() != null) {
            builder.append("(");
        }

        // type itself
        if (basicType.getBuiltinType() != null) {
            DDocHtmlUtil.appendStyledSpan(
                builder,
                DColor.KEYWORD.getTextAttributesKey(),
                basicType.getBuiltinType().getText()
            );
        }
        else if (basicType.getType() != null) {
            appendType(builder, basicType.getType());
        }
        else if (basicType.getQualifiedIdentifier() != null) {
            appendQualifiedIdentifier(builder, basicType.getQualifiedIdentifier());
        }

        if (basicType.getOP_PAR_RIGHT() != null) {
            builder.append(")");
        }

        // suffix
        if (!typeSuffixes.isEmpty()) {
            if (typeSuffixes.size() > 1
                && (typeSuffixes.get(0).getOP_ASTERISK() == null && typeSuffixes.get(0).getOP_BRACKET_LEFT() == null)) {
                // keep star and [ attached to their type
                builder.append(" ");
            }
            builder.append(typeSuffixes.stream().map(it -> {
                StringBuilder parametersBuilder = new StringBuilder();
                if (it.getKW_DELEGATE() != null) {
                    DDocHtmlUtil.appendStyledSpan(
                        parametersBuilder,
                        DColor.KEYWORD.getTextAttributesKey(),
                        "delegate"
                    );
                }
                if (it.getKW_FUNCTION() != null) {
                    DDocHtmlUtil.appendStyledSpan(
                        parametersBuilder,
                        DColor.KEYWORD.getTextAttributesKey(),
                        "function"
                    );
                }
                if (it.getParameters() != null) {
                    parametersBuilder.append("(");
                    appendParametersSignature(parametersBuilder, it.getParameters());
                    parametersBuilder.append(")");
                    return parametersBuilder.toString();
                }
                return it.getText();
            }).collect(Collectors.joining("")));
        }
        return builder;
    }

    @RequiredReadAction
    private StringBuilder appendType(StringBuilder builder, @Nullable DLanguageType element) {
        if (element == null) {
            return builder;
        }
        if (element.getKW_SHARED() != null) {
            builder.append(element.getKW_SHARED().getText());
            builder.append(" ");
        }
        if (element.getKW_CONST() != null) {
            builder.append(element.getKW_CONST().getText());
            builder.append(" ");
        }
        if (element.getKW_IMMUTABLE() != null) {
            builder.append(element.getKW_IMMUTABLE().getText());
            builder.append(" ");
        }
        if (element.getKW_INOUT() != null) {
            builder.append(element.getKW_INOUT().getText());
            builder.append(" ");
        }
        if (element.getBasicType() != null) {
            appendType(builder, element.getBasicType(), element.getTypeSuffixs());
        }
        else {
            builder.append(element.getText());
        }
        return builder;
    }

    @RequiredReadAction
    private void appendQualifiedIdentifier(StringBuilder builder, DLanguageQualifiedIdentifier element) {
        if (element.getQualifiedIdentifier() != null) {
            appendQualifiedIdentifier(builder, element.getQualifiedIdentifier());
            builder.append(".");
        }
        if (element.getIdentifier() != null) {
            builder.append(element.getIdentifier().getText());
        }
        else {
            // template instance
            builder.append(Objects.requireNonNull(Objects.requireNonNull(element.getTemplateInstance()).getIdentifier()).getText());
            DLanguageTemplateArguments templateArguments = element.getTemplateInstance().getTemplateArguments();
            if (templateArguments != null && templateArguments.getTemplateSingleArgument() != null) {
                builder.append("!");
                appendTemplateSingleArgument(builder, templateArguments.getTemplateSingleArgument());
            }
            else {
                builder.append("!(");
                appendTemplateArguments(builder, Objects.requireNonNull(templateArguments));
                builder.append(")");
            }
        }

        if (element.getOP_BRACKET_LEFT() != null) {
            builder.append("[");
        }
        if (element.getExpression() != null) {
            builder.append(element.getExpression().getText());
        }
        if (element.getOP_BRACKET_RIGHT() != null) {
            builder.append("]");
        }
    }

    @RequiredReadAction
    private void appendTemplateArguments(StringBuilder builder, DLanguageTemplateArguments element) {
        if (element.getTemplateArgumentList() == null) {
            return;
        }
        builder.append(element.getTemplateArgumentList().getTemplateArguments().stream().map((DLanguageTemplateArgument it) -> {
            StringBuilder argumentBuilder = new StringBuilder();
            appendType(argumentBuilder, it.getType());
            if (it.getAssignExpression() != null) {
                argumentBuilder.append(it.getAssignExpression().getText());
            }
            return argumentBuilder.toString();
        }).collect(Collectors.joining(", ")));
    }

    @RequiredReadAction
    private void appendTemplateSingleArgument(StringBuilder builder, DLanguageTemplateSingleArgument element) {
        if (element.getKW_FALSE() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "false");
        }
        else if (element.getKW_TRUE() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "true");
        }
        else if (element.getKW_NULL() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "null");
        }
        else if (element.getKW_SUPER() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "super");
        }
        else if (element.getKW_THIS() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), "this");
        }
        else if (element.getBuiltinType() != null) {
            DDocHtmlUtil.appendStyledSpan(builder, DColor.KEYWORD.getTextAttributesKey(), element.getBuiltinType().getText());
        }
        else {
            builder.append(element.getText());
        }
    }

    /**
     * {@code element?.text}
     */
    @RequiredReadAction
    private static @Nullable String textOf(@Nullable PsiElement element) {
        return element == null ? null : element.getText();
    }
}

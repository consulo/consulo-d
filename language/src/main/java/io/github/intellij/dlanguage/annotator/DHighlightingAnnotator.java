package io.github.intellij.dlanguage.annotator;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.psi.PsiUtilCore;
import io.github.intellij.dlanguage.colors.DColor;
import io.github.intellij.dlanguage.psi.DLanguageAtAttribute;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageReferenceExpression;
import io.github.intellij.dlanguage.psi.DLanguageTemplateAliasParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateInstance;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageTemplateSingleArgument;
import io.github.intellij.dlanguage.psi.DLanguageTemplateThisParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateTypeParameter;
import io.github.intellij.dlanguage.psi.DLanguageTemplateValueParameter;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.TemplateParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;

import org.jspecify.annotations.Nullable;

import java.util.Set;

public class DHighlightingAnnotator implements Annotator {
    // TODO find a better place for this variable
    /**
     * Contains the "standard aliased types" as defined in the specs.
     * The standards aliased types are not native types but are defined in object.d
     */
    public static final Set<String> DlangAliasedTypes = Set.of("size_t", "ptrdiff_t", "noreturn", "string", "wstring", "dstring");

    private record Highlight(@Nullable TextRange part, DColor color) {
    }

    @Override
    @RequiredReadAction
    public void annotate(PsiElement element, AnnotationHolder holder) {
        Highlight highlight;
        if (element instanceof DLanguageAtAttribute) {
            highlight = new Highlight(element.getTextRange(), DColor.AT_ATTRIBUTE);
        }
        else if (element instanceof DLanguageModuleDeclaration moduleDeclaration) {
            DLanguageIdentifierChain identifierChain = moduleDeclaration.getIdentifierChain();
            highlight = new Highlight(identifierChain == null ? null : identifierChain.getTextRange(), DColor.MODULE_DEFINITION);
        }
        else if (element instanceof DLanguageReferenceExpression || element instanceof DLanguageQualifiedIdentifier) {
            highlight = highlightReference(element.getReference());
        }
        else if (element instanceof DLanguageTemplateSingleArgument || element instanceof TemplateParameter) {
            highlight = highlightNotReference(element);
        }
        else if (PsiUtilCore.getElementType(element) == DlangTypes.ID) {
            highlight = highlightIdentifier(element);
        }
        else {
            highlight = null;
        }
        if (highlight == null) {
            return;
        }

        TextRange partToHighlight = highlight.part();
        if (partToHighlight == null) {
            return;
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(partToHighlight).textAttributes(highlight.color().getTextAttributesKey()).create();
    }

    @RequiredReadAction
    private @Nullable Highlight highlightReference(@Nullable PsiReference reference) {
        if (reference == null) {
            return null;
        }
        PsiElement identifier = reference.getElement();

        PsiElement result = reference.resolve();
        if (result == null) {
            return null;
        }

        DColor color = colorForReferenced(result);
        if (color == null) {
            return null;
        }
        TextRange part = partToHighlight(identifier);
        if (part == null) {
            return null;
        }

        return new Highlight(part, color);
    }

    @RequiredReadAction
    private @Nullable Highlight highlightNotReference(PsiElement element) {
        DColor color = colorFor(element);
        if (color == null) {
            return null;
        }
        TextRange part = partToHighlight(element);
        if (part == null) {
            return null;
        }

        return new Highlight(part, color);
    }

    @RequiredReadAction
    private @Nullable Highlight highlightIdentifier(PsiElement element) {
        PsiElement parent = element.getParent();
        DColor color;
        if (parent instanceof Declaration) {
            color = colorForDeclarationIdentifier(parent);
        }
        else if (parent instanceof DLanguageTemplateInstance) {
            // don’t colorize templated class/struct/union instantiations as function calls
            if (PsiTreeUtil.getParentOfType(parent, DLanguageBasicType.class, true, Declaration.class) == null) {
                color = DColor.FUNCTION_CALL;
            }
            else {
                color = null;
            }
        }
        else {
            color = null;
        }
        if (color == null) {
            return null;
        }

        return new Highlight(element.getTextRange(), color);
    }

    private @Nullable DColor colorFor(PsiElement element) {
        if (element instanceof DLanguageTemplateSingleArgument
            || element instanceof DLanguageTemplateTypeParameter
            || element instanceof TemplateParameter) {
            return DColor.TYPE_PARAMETER;
        }
        return null;
    }

    /**
     * Return the color for an identifier that is a 'declaration'
     * @param element: the reference of the element to color
     */
    private @Nullable DColor colorForDeclarationIdentifier(PsiElement element) {
        if (element instanceof DLanguageClassDeclaration) {
            return DColor.CLASS_DEFINITION;
        }
        if (element instanceof DLanguageInterfaceDeclaration) {
            return DColor.INTERFACE_DEFINITION;
        }
        if (element instanceof DLanguageStructDeclaration) {
            return DColor.STRUCT_DEFINITION;
        }
        if (element instanceof DLanguageUnionDeclaration) {
            return DColor.UNION_DEFINITION;
        }
        if (element instanceof DLanguageEnumDeclaration) {
            return DColor.ENUM_DEFINITION;
        }
        if (element instanceof DLanguageFunctionDeclaration
            || element instanceof DLanguageTemplateDeclaration
            || element instanceof DLanguageTemplateMixinDeclaration) {
            return DColor.FUNCTION_DEFINITION;
        }
        return null;
    }

    /**
     * Return the color for a PsiElement that has `element` as reference
     * @param element: the reference of the element to color
     */
    @RequiredReadAction
    private @Nullable DColor colorForReferenced(PsiElement element) {
        if (element instanceof DLanguageTemplateSingleArgument
            || element instanceof DLanguageTemplateTypeParameter
            || element instanceof TemplateParameter) {
            return DColor.TYPE_PARAMETER;
        }
        if (element instanceof DLanguageFunctionDeclaration functionDeclaration) {
            // We ignore properties here because they behave like struct/class field
            if (functionDeclaration.isProperty()) {
                return null;
            }
            else {
                return DColor.FUNCTION_CALL;
            }
        }
        if (element instanceof DLanguageTemplateDeclaration || element instanceof DLanguageTemplateMixinDeclaration) {
            return DColor.FUNCTION_CALL;
        }
        if (element instanceof DLanguageAliasInitializer aliasInitializer) {
            // Colorize standard alias types as if they were keywords
            PsiElement identifier = aliasInitializer.getIdentifier();
            if (identifier != null && DlangAliasedTypes.contains(identifier.getText())) {
                return DColor.KEYWORD;
            }
            else {
                return null;
            }
        }
        return null;
    }

    @RequiredReadAction
    private @Nullable TextRange partToHighlight(PsiElement element) {
        PsiElement name;
        if (element instanceof DLanguageTemplateTypeParameter it) {
            name = it.getIdentifier();
        }
        else if (element instanceof DLanguageTemplateValueParameter it) {
            name = it.getIdentifier();
        }
        else if (element instanceof DLanguageTemplateAliasParameter it) {
            name = it.getIdentifier();
        }
        else if (element instanceof DLanguageTemplateThisParameter it) {
            DLanguageTemplateTypeParameter templateTypeParameter = it.getTemplateTypeParameter();
            name = templateTypeParameter == null ? null : templateTypeParameter.getIdentifier();
        }
        else if (element instanceof DLanguageReferenceExpression it) {
            name = it.getIdentifier();
        }
        else if (element instanceof DLanguageTemplateSingleArgument it) {
            name = it.getIdentifier();
        }
        else if (element instanceof DLanguageQualifiedIdentifier it) {
            name = it.getIdentifier();
        }
        else {
            name = element;
        }

        return name == null ? null : name.getTextRange();
    }
}

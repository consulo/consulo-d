package io.github.intellij.dlanguage.presentation;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAnonymousEnumDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAutoDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageQualifiedIdentifier;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedVariableDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixin;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DLanguageTypeSuffix;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.VariableDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageAliasInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.named.DLanguageDestructor;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.structure.Visibility;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Top-level declarations of upstream's {@code presentation/Utils.kt}.
 */
public final class UtilsKt {
    private UtilsKt() {
    }

    public static @Nullable String presentableName(@Nullable PsiElement psi) {
        if (psi instanceof DLanguageConstructor) {
            return "this";
        }
        else if (psi instanceof DLanguageDestructor) {
            return "~this";
        }
        else if (psi instanceof PsiNamedElement namedElement) {
            return namedElement.getName();
        }
        else if (psi instanceof DLanguageAnonymousEnumDeclaration) {
            return "<unnamed>";
        }
        else if (psi instanceof DLanguageAutoDeclaration autoDeclaration) {
            List<DLanguageAutoAssignment> autoAssignments = autoDeclaration.getAutoAssignments();
            return autoAssignments.isEmpty() ? null : autoAssignments.get(0).getName();
        }
        else if (psi instanceof DLanguageSpecifiedVariableDeclaration specifiedVariableDeclaration) {
            List<DLanguageIdentifierInitializer> identifierInitializers = specifiedVariableDeclaration.getIdentifierInitializers();
            if (identifierInitializers.isEmpty()) {
                return null;
            }
            PsiElement identifier = identifierInitializers.get(0).getIdentifier();
            return identifier == null ? null : identifier.getText();
        }
        else if (psi instanceof DLanguageAliasDeclaration aliasDeclaration) {
            List<String> names = new ArrayList<>();
            for (DLanguageAliasInitializer aliasInitializer : aliasDeclaration.getAliasInitializers()) {
                String name = aliasInitializer.getName();
                if (name != null) {
                    names.add(name);
                }
            }
            return String.join(", ", names);
        }
        else if (psi instanceof DLanguageType type) {
            DLanguageBasicType basicType = type.getBasicType();
            DLanguageQualifiedIdentifier qualifiedIdentifier = basicType == null ? null : basicType.getQualifiedIdentifier();
            if (qualifiedIdentifier != null) {
                List<DLanguageTypeSuffix> typeSuffixs = type.getTypeSuffixs();
                if (!typeSuffixs.isEmpty()) {
                    DLanguageTypeSuffix first = typeSuffixs.get(0);
                    if (first.getKW_DELEGATE() != null || first.getKW_FUNCTION() != null) {
                        return qualifiedIdentifier.getText() + " " + presentableName(first);
                    }
                    else {
                        return qualifiedIdentifier.getText() + presentableName(first);
                    }
                }
                else {
                    return qualifiedIdentifier.getText();
                }
            }
            else {
                return type.getText();
            }
        }
        else if (psi instanceof DLanguageTypeSuffix typeSuffix) {
            PsiElement kwDelegate = typeSuffix.getKW_DELEGATE();
            PsiElement kwFunction = typeSuffix.getKW_FUNCTION();
            if (kwDelegate != null) {
                return kwDelegate.getText() + "(" + String.join(", ", parameterTypeNames(Objects.requireNonNull(typeSuffix.getParameters()))) + ")";
            }
            else if (kwFunction != null) {
                return kwFunction.getText() + "(" + String.join(", ", parameterTypeNames(Objects.requireNonNull(typeSuffix.getParameters()))) + ")";
            }
            else {
                return typeSuffix.getText();
            }
        }
        else if (psi instanceof DLanguageTemplateMixinDeclaration templateMixinDeclaration) {
            // mixin Template() { ... }
            PsiElement identifier = templateMixinDeclaration.getIdentifier();
            return identifier == null ? null : identifier.getText();
        }
        else if (psi instanceof DLanguageTemplateMixin templateMixinStatement) {
            // mixin Template!();
            PsiElement mixinTemplateName = templateMixinStatement.getMixinTemplateName();
            return mixinTemplateName == null ? null : mixinTemplateName.getText();
        }
        else {
            return String.valueOf(psi);
        }
    }

    private static List<String> parameterTypeNames(DLanguageParameters parameters) {
        List<String> names = new ArrayList<>();
        for (DLanguageParameter parameter : parameters.getParameters()) {
            String name = presentableName(parameter.getType());
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    public static @Nullable Image getPresentationIcon(@Nullable PsiElement psi) {
        if (psi instanceof DLanguageClassDeclaration) {
            return DLanguage.Icons.NODE_CLASS;
        }
        else if (psi instanceof DLanguageInterfaceDeclaration) {
            return DLanguage.Icons.NODE_INTERFACE;
        }
        else if (psi instanceof DLanguageFunctionDeclaration functionDeclaration) {
            if (psiElementIsGetter(functionDeclaration)) {
                return DLanguage.Icons.NODE_PROPERTY_GETTER;
            }
            else if (psiElementIsSetter(functionDeclaration)) {
                return DLanguage.Icons.NODE_PROPERTY_SETTER;
            }
            else if (psiElementIsProperty(functionDeclaration)) {
                return DLanguage.Icons.NODE_PROPERTY;
            }
            else if (psiElementIsMethod(functionDeclaration)) {
                return DLanguage.Icons.NODE_METHOD;
            }
            else {
                return DLanguage.Icons.NODE_FUNCTION;
            }
        }
        else if (psi instanceof DLanguageConstructor) {
            return DLanguage.Icons.NODE_METHOD;
        }
        else if (psi instanceof DLanguageDestructor) {
            return DLanguage.Icons.NODE_METHOD;
        }
        else if (psi instanceof DLanguageEnumDeclaration) {
            return DLanguage.Icons.NODE_ENUM;
        }
        else if (psi instanceof DLanguageAnonymousEnumDeclaration) {
            return DLanguage.Icons.NODE_ENUM;
        }
        else if (psi instanceof DLanguageEnumMember) {
            return DLanguage.Icons.NODE_ENUM_MEMBER;
        }
        else if (psi instanceof DLanguageStructDeclaration) {
            return DLanguage.Icons.NODE_STRUCT;
        }
        else if (psi instanceof DLanguageUnionDeclaration) {
            return DLanguage.Icons.NODE_UNION;
        }
        else if (psi instanceof VariableDeclaration) {
            return DLanguage.Icons.NODE_FIELD;
        }
        else if (psi instanceof DLanguageAliasDeclaration) {
            return DLanguage.Icons.NODE_ALIAS;
        }
        else if (psi instanceof DLanguageTemplateMixinDeclaration) {
            return DLanguage.Icons.NODE_MIXINDECL; // mixin Template() { ... }
        }
        else if (psi instanceof DLanguageTemplateMixin) {
            return DLanguage.Icons.NODE_MIXIN; // mixin Template!();
        }
        else if (psi instanceof DLanguageTemplateDeclaration) {
            return DLanguage.Icons.NODE_FUNCTION;
        }
        else if (psi instanceof DlangPsiFile) {
            return DLanguage.Icons.FILE;
        }
        else {
            return null;
        }
    }

    public static boolean psiElementIsProperty(DLanguageFunctionDeclaration psi) {

        /*parent.attributes.forEach {
            if (it.atAttribute?.identifier?.name == "property")
                return true
        }*/

        return false;
    }

    public static boolean psiElementIsGetter(DLanguageFunctionDeclaration psi) {
        if (!psiElementIsProperty(psi)) {
            return false;
        }
        DLanguageParameters parameters = psi.getParameters();
        return parameters != null && parameters.getParameters().size() == 0;
    }

    public static boolean psiElementIsSetter(DLanguageFunctionDeclaration psi) {
        if (!psiElementIsProperty(psi)) {
            return false;
        }
        DLanguageParameters parameters = psi.getParameters();
        return parameters != null && parameters.getParameters().size() == 1;
    }

    public static boolean psiElementIsMethod(@Nullable PsiElement psi) {
        if (psi == null) {
            return false;
        }

        PsiElement parent = psi.getParent();

        while (parent != null) {
            boolean isMethod = parent instanceof DLanguageStructDeclaration || parent instanceof DLanguageUnionDeclaration;

            if (isMethod) {
                return true;
            }

            parent = parent.getParent();
        }

        return false;
    }

    public static String psiElementShortText(PsiElement psi) {
        String text = psi.getText().trim();
        int maxLength = 40;

        if (text.length() > maxLength) {
            return text.subSequence(0, maxLength - 3) + " ...";
        }
        else {
            return text;
        }
    }

    public static Visibility psiElementGetVisibility(@Nullable PsiElement psi) {
        if (psi == null) {
            return Visibility.NONE;
        }

        return extractNodeVisibility(psi);
    }

    private static Visibility extractNodeVisibility(@Nullable PsiElement psi) {
        if (psi == null) {
            return Visibility.NONE;
        }

        if (psi instanceof DLanguageClassDeclaration) {
            return Visibility.PUBLIC;
        }
        else if (psi instanceof DLanguageInterfaceDeclaration) {
            return Visibility.PUBLIC;
        }
        else if (psi instanceof DLanguageStructDeclaration) {
            return Visibility.PUBLIC;
        }
        else if (psi instanceof DLanguageEnumDeclaration) {
            return Visibility.PUBLIC;
        }
        else if (psi instanceof DLanguageUnionDeclaration) {
            return Visibility.PUBLIC;
        }
        /*is Declaration -> {
            psi.attributes
                .map { fromPsiAttribute(it) }
                .filter { it != Visibility.NONE }
                .forEach { return it }

            var sibling = psi.prevSibling

            while (sibling != null) {
                if (sibling is Declaration && sibling.attributeDeclaration != null) {
                    sibling.attributes
                        .map { fromPsiAttribute(it) }
                        .filter { it != Visibility.NONE }
                        .forEach { return it }
                }

                sibling = sibling.prevSibling
            }

            return extractNodeVisibility(psi.parent)
        }*/
        else {
            return extractNodeVisibility(psi.getParent());
        }
    }
}

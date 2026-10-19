package io.github.intellij.dlanguage.structure;

import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.Navigatable;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAnonymousEnumDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageAttributeSpecifier;
import io.github.intellij.dlanguage.psi.DLanguageAutoDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageConditionalDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageDeclarationBlock;
import io.github.intellij.dlanguage.psi.DLanguageEmptyDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageEnumBody;
import io.github.intellij.dlanguage.psi.DLanguageImportDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageInitializer;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedVariableDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageStorageClass;
import io.github.intellij.dlanguage.psi.DLanguageStructBody;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DLanguageTypeSuffix;
import io.github.intellij.dlanguage.psi.DLanguageUnittest;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.interfaces.Declaration;
import io.github.intellij.dlanguage.psi.interfaces.VariableDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.intellij.dlanguage.presentation.UtilsKt.getPresentationIcon;
import static io.github.intellij.dlanguage.presentation.UtilsKt.presentableName;
import static io.github.intellij.dlanguage.presentation.UtilsKt.psiElementGetVisibility;
import static io.github.intellij.dlanguage.presentation.UtilsKt.psiElementIsGetter;
import static io.github.intellij.dlanguage.presentation.UtilsKt.psiElementIsSetter;
import static io.github.intellij.dlanguage.presentation.UtilsKt.psiElementShortText;

public class DStructureViewElement implements StructureViewTreeElement {
    private final PsiElement myElement;

    public DStructureViewElement(PsiElement element) {
        myElement = element;
    }

    public PsiElement getElement() {
        return myElement;
    }

    private @Nullable String variableDeclarationAttributes(VariableDeclaration element) {
        List<String> allowedAttributes = List.of("const", "enum", "immutable");

        if (element instanceof DLanguageAutoDeclaration autoDeclaration) {
            List<DLanguageStorageClass> attributes = autoDeclaration.getStorageClasss();

            for (DLanguageStorageClass attribute : attributes) {
                if (allowedAttributes.contains(attribute.getText())) {
                    return attribute.getText();
                }
            }

        }
        else if (element instanceof DLanguageSpecifiedVariableDeclaration specifiedVariableDeclaration) {
            StringBuilder attributes = new StringBuilder();
            for (DLanguageStorageClass it : specifiedVariableDeclaration.getStorageClasss()) {
                if (allowedAttributes.contains(it.getText())) {
                    attributes.append(it.getText()).append(" ");
                }
            }
            return attributes.toString().trim();
        }

        return null;
    }

    @Override
    public ItemPresentation getPresentation() {
        StringBuilder presentation = new StringBuilder();

        // Insert variables attributes
        if (myElement instanceof VariableDeclaration variableDeclaration) {
            String attributes = variableDeclarationAttributes(variableDeclaration);

            if (attributes != null) {
                presentation.append(attributes);

                if (!attributes.isEmpty()) {
                    presentation.append(" ");
                }
            }
        }

        presentation.append(presentableName(myElement));

        // Additional information
        if (myElement instanceof DLanguageFunctionDeclaration functionDeclaration) {
            if (psiElementIsGetter(functionDeclaration)) {
                presentation.append((" : " + text(functionDeclaration.getBasicType()) + " " + joinTexts(functionDeclaration.getTypeSuffixes())).stripTrailing());
            }
            else if (psiElementIsSetter(functionDeclaration)) {
                DLanguageParameters parametersNode = functionDeclaration.getParameters();
                appendCommaList(presentation, parameterTypeNames(parametersNode));
            }
            else {
                DLanguageParameters parametersNode = functionDeclaration.getParameters();
                appendCommaList(presentation, parameterTypeNames(parametersNode));
                presentation.append((" : " + text(functionDeclaration.getBasicType()) + " " + joinTexts(functionDeclaration.getTypeSuffixes())).stripTrailing());
            }
        }
        else if (myElement instanceof DLanguageConstructor constructor) {
            DLanguageParameters parametersNode = constructor.getParameters();
            appendCommaList(presentation, parameterTypeNames(parametersNode));
        }
        else if (myElement instanceof DLanguageSpecifiedVariableDeclaration specifiedVariableDeclaration) {
            presentation.append((" : " + text(specifiedVariableDeclaration.getBasicType()) + " " + joinTexts(specifiedVariableDeclaration.getTypeSuffixs())).stripTrailing());

            List<DLanguageIdentifierInitializer> identifierInitializers = specifiedVariableDeclaration.getIdentifierInitializers();
            DLanguageInitializer initializer = identifierInitializers.isEmpty() ? null : identifierInitializers.get(0).getInitializer();

            if (initializer != null) {
                presentation.append(" = ").append(psiElementShortText(initializer));
            }
        }
        else if (myElement instanceof DLanguageAutoDeclaration autoDeclaration) {
            List<DLanguageAutoAssignment> parts = autoDeclaration.getAutoAssignments();
            DLanguageAutoAssignment declPart = parts.isEmpty() ? null : parts.get(0);

            if (declPart != null) {
                DLanguageInitializer initializer = declPart.getInitializer();

                if (initializer != null) {
                    presentation.append(" = ").append(psiElementShortText(initializer));
                }
            }
        }
        else if (myElement instanceof DLanguageAliasDeclaration aliasDeclaration) {
            if (aliasDeclaration.getType() != null) {
                presentation.append(" = ").append(psiElementShortText(Objects.requireNonNull(aliasDeclaration.getType())));
            }
        }
        else if (myElement instanceof DLanguageEnumMember enumMember) {
            String elementType = "";
            PsiElement parent = enumMember.getParent();
            if (parent instanceof DLanguageAnonymousEnumDeclaration anonymousEnumDeclaration) {
                DLanguageType type = anonymousEnumDeclaration.getType();
                elementType = type == null ? "" : type.getText();
            }
            else if (parent instanceof DLanguageEnumBody && parent.getParent() instanceof DLanguageEnumDeclaration enumDeclaration) {
                DLanguageType type = enumDeclaration.getType();
                elementType = type != null ? type.getText() : Objects.requireNonNull(enumDeclaration.getIdentifier()).getText();
            }
            presentation.append(": ").append(elementType);
        }

        Image icon = getPresentationIcon(myElement);

        if (icon == null) {
            return new PresentationData(LocalizeValue.of(presentation.toString()), null, icon, null);
        }

        Visibility visibility = psiElementGetVisibility(myElement);

        if (visibility != Visibility.NONE) {
            icon = addVisibilityToIcon(icon, visibility);
        }

        return new PresentationData(LocalizeValue.of(presentation.toString()), null, icon, null);
    }

    private static void appendCommaList(StringBuilder builder, @Nullable List<String> xs) {
        if (xs == null) {
            return;
        }

        builder.append('(');
        builder.append(String.join(", ", xs));
        builder.append(')');
    }

    private static @Nullable List<String> parameterTypeNames(@Nullable DLanguageParameters parametersNode) {
        if (parametersNode == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (DLanguageParameter parameter : parametersNode.getParameters()) {
            String name = presentableName(parameter.getType());
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    private static @Nullable String text(@Nullable DLanguageBasicType basicType) {
        return basicType == null ? null : basicType.getText();
    }

    private static String joinTexts(List<DLanguageTypeSuffix> typeSuffixes) {
        List<String> texts = new ArrayList<>();
        for (DLanguageTypeSuffix typeSuffix : typeSuffixes) {
            texts.add(typeSuffix.getText());
        }
        return String.join(" ", texts);
    }

    @Override
    public TreeElement[] getChildren() {
        List<TreeElement> treeElements = new ArrayList<>();

        for (PsiElement it : findChildrenNodes(myElement)) {
            treeElements.add(new DStructureViewElement(it));
        }

        return treeElements.toArray(TreeElement.EMPTY_ARRAY);
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (myElement instanceof Navigatable navigatable) {
            navigatable.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return myElement instanceof Navigatable navigatable && navigatable.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return myElement instanceof Navigatable navigatable && navigatable.canNavigateToSource();
    }

    private List<PsiElement> findChildrenNodes(@Nullable PsiElement psi) {
        List<? extends PsiElement> elements;
        if (psi instanceof DlangPsiFile) {
            List<PsiElement> declarations = new ArrayList<>();
            for (PsiElement it : psi.getChildren()) {
                if (!(it instanceof DLanguageModuleDeclaration) && it instanceof Declaration) {
                    declarations.add(it);
                }
            }
            elements = declarations;
        }
        else if (psi instanceof DLanguageClassDeclaration classDeclaration) {
            elements = declarationsOf(classDeclaration.getStructBody());
        }
        else if (psi instanceof DLanguageInterfaceDeclaration interfaceDeclaration) {
            elements = declarationsOf(interfaceDeclaration.getStructBody());
        }
        else if (psi instanceof DLanguageFunctionDeclaration) {
            elements = Collections.emptyList();
        }
        else if (psi instanceof DLanguageEnumDeclaration enumDeclaration) {
            DLanguageEnumBody enumBody = enumDeclaration.getEnumBody();
            elements = enumBody == null ? Collections.emptyList() : enumBody.getEnumMembers();
        }
        else if (psi instanceof DLanguageAnonymousEnumDeclaration anonymousEnumDeclaration) {
            elements = anonymousEnumDeclaration.getEnumMembers();
        }
        else if (psi instanceof DLanguageStructDeclaration structDeclaration) {
            elements = declarationsOf(structDeclaration.getStructBody());
        }
        else if (psi instanceof DLanguageUnionDeclaration unionDeclaration) {
            elements = declarationsOf(unionDeclaration.getStructBody());
        }
        else if (psi instanceof DLanguageConstructor) {
            elements = Collections.emptyList();
        }
        else if (psi instanceof VariableDeclaration) {
            elements = Collections.emptyList();
        }
        else if (psi instanceof DLanguageAliasDeclaration) {
            elements = Collections.emptyList();
        }
        else if (psi instanceof DLanguageTemplateMixinDeclaration templateMixinDeclaration) {
            elements = templateMixinDeclaration.getDeclarations();
        }
        else if (psi instanceof DLanguageConditionalDeclaration conditionalDeclaration) {
            elements = conditionalDeclaration.getDeclarations();
        }
        else if (psi instanceof DLanguageAttributeSpecifier attributeSpecifier) {
            elements = declarationsOf(attributeSpecifier.getDeclarationBlock());
        }
        else {
            elements = Collections.emptyList();
        }
        return findInnerDeclarations(elements);
    }

    private static List<Declaration> declarationsOf(@Nullable DLanguageStructBody structBody) {
        return structBody == null ? Collections.emptyList() : structBody.getDeclarations();
    }

    private static List<Declaration> declarationsOf(@Nullable DLanguageDeclarationBlock declarationBlock) {
        return declarationBlock == null ? Collections.emptyList() : declarationBlock.getDeclarations();
    }

    private List<PsiElement> findInnerDeclarations(List<? extends PsiElement> declarations) {
        List<PsiElement> result = new ArrayList<>();
        for (PsiElement it : declarations) {
            if (it instanceof DLanguageImportDeclaration || it instanceof DLanguageEmptyDeclaration || it instanceof DLanguageUnittest) {
                continue;
            }
            if (it instanceof DLanguageAttributeSpecifier attributeSpecifier) {
                result.addAll(findInnerDeclarations(declarationsOf(attributeSpecifier.getDeclarationBlock())));
            }
            else if (it instanceof DLanguageConditionalDeclaration conditionalDeclaration) {
                result.addAll(findInnerDeclarations(conditionalDeclaration.getDeclarations()));
            }
            else {
                result.add(it);
            }
        }
        return result;
    }

    @Override
    public Object getValue() {
        return myElement;
    }

    private Image addVisibilityToIcon(Image icon, Visibility visibility) {
        Image visibilityIcon = switch (visibility) {
            case PUBLIC -> PlatformIconGroup.nodesC_public();
            case PRIVATE -> PlatformIconGroup.nodesC_private();
            case PROTECTED -> PlatformIconGroup.nodesC_protected();
            case PACKAGE -> PlatformIconGroup.nodesC_plocal();
            case NONE -> PlatformIconGroup.nodesC_public();
        };
        return ImageEffects.appendRight(icon, visibilityIcon);
    }
}

package io.github.intellij.dlanguage.structure;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.ui.navigationBar.StructureAwareNavBarModelExtension;
import consulo.language.psi.PsiElement;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixinDeclaration;
import io.github.intellij.dlanguage.psi.interfaces.UserDefinedType;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageTemplateDeclaration;
import org.jspecify.annotations.Nullable;

import static io.github.intellij.dlanguage.presentation.UtilsKt.getPresentationIcon;

@ExtensionImpl
public class DLanguageStructureAwareNavbar extends StructureAwareNavBarModelExtension {

    @Override
    protected Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public @Nullable String getPresentableText(Object element) {
        if (element instanceof UserDefinedType userDefinedType) {
            return userDefinedType.getName();
        }
        if (element instanceof DLanguageFunctionDeclaration functionDeclaration) {
            return functionDeclaration.getName();
        }
        if (element instanceof DLanguageTemplateDeclaration templateDeclaration) {
            return templateDeclaration.getName();
        }
        if (element instanceof DLanguageTemplateMixinDeclaration templateMixinDeclaration) {
            PsiElement identifier = templateMixinDeclaration.getIdentifier();
            return identifier == null ? null : identifier.getText();
        }
        return null;
    }

    @Override
    public @Nullable Image getIcon(Object element) {
        return element instanceof PsiElement psiElement
            ? getPresentationIcon(psiElement)
            : super.getIcon(element);
    }

    @Override
    public @Nullable PsiElement getParent(PsiElement psiElement) {
        PsiElement parent = super.getParent(psiElement);
        if (parent != null) {
            return parent;
        }
        return psiElement.getContainingFile();
    }
}

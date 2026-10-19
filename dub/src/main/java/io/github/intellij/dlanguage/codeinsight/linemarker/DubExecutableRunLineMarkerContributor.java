package io.github.intellij.dlanguage.codeinsight.linemarker;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.icon.ExecutionIconGroup;
import consulo.execution.lineMarker.ExecutorAction;
import consulo.execution.lineMarker.RunLineMarkerContributor;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageBasicType;
import io.github.intellij.dlanguage.psi.DLanguageParameters;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@ExtensionImpl
public class DubExecutableRunLineMarkerContributor extends RunLineMarkerContributor {
    @Override
    @RequiredReadAction
    public @Nullable Info getInfo(PsiElement element) {
        if (!isDMainFunction(element)) {
            return null;
        }

        return new Info(
            ExecutionIconGroup.gutterRun(),
            null,
            ExecutorAction.getActions()
        );
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @RequiredReadAction
    private boolean isDMainFunction(PsiElement element) {
        if (!(PsiUtilCore.getElementType(element) == DlangTypes.ID && element.getParent() instanceof DLanguageFunctionDeclaration
            && element.getParent().getParent() instanceof DlangPsiFile && element.getText().equals("main"))) {
            return false;
        }
        DLanguageFunctionDeclaration funcDecl = (DLanguageFunctionDeclaration) element.getParent();
        // check return type
        DLanguageBasicType basicType = funcDecl.getBasicType();
        if (!(funcDecl.isAuto() ||
            basicType != null && List.of("void", "int", "noreturn").contains(basicType.getText()))) {
            return false;
        }
        DLanguageParameters parameters = funcDecl.getParameters();
        if (parameters == null) {
            return false;
        }
        if (parameters.getParameters().size() >= 2) {
            return false;
        }

        if (parameters.getParameters().isEmpty()) {
            return true;
        }
        DLanguageParameter param = parameters.getParameters().get(0);
        DLanguageType type = param.getType();
        if (type == null) {
            return false;
        }
        DLanguageBasicType paramBasicType = type.getBasicType();
        return paramBasicType != null && paramBasicType.getText().equals("string")
            && type.getTypeSuffixs().size() == 1 && type.getTypeSuffixs().get(0).getText().equals("[]");
    }
}

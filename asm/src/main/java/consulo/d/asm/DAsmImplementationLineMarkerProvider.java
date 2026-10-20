package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.asm.language.impl.psi.AsmLabelElement;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.gutter.RelatedItemLineMarkerInfo;
import consulo.language.editor.gutter.RelatedItemLineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;
import consulo.asm.language.icon.AsmIconGroup;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.Collection;
import java.util.List;

/**
 * {@code extern (C) int add(int, int);}: gutter icon to the assembly label implementing it.
 */
@ExtensionImpl
public class DAsmImplementationLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @RequiredReadAction
    @Override
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element.getParent() instanceof DLanguageFunctionDeclaration function)
            || function.getIdentifier() != element
            || !DAsmUtil.isExternWithoutBody(function)) {
            return;
        }
        List<AsmLabelElement> implementations = DAsmUtil.findAsmImplementations(function);
        if (implementations.isEmpty()) {
            return;
        }
        result.add(NavigationGutterIconBuilder.create(AsmIconGroup.asm())
            .setTargets(implementations)
            .setTooltipText(DLocalize.asmImplementationTooltip())
            .createLineMarkerInfo(element));
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

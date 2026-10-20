package consulo.d.asm;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.asm.language.AsmLanguage;
import consulo.asm.language.impl.psi.AsmLabelElement;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.editor.gutter.RelatedItemLineMarkerInfo;
import consulo.language.editor.gutter.RelatedItemLineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.Collection;
import java.util.List;

/**
 * {@code add:} in an assembly file: gutter icon to the {@code extern} D function it implements.
 */
@ExtensionImpl
public class AsmDDeclarationLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @RequiredReadAction
    @Override
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        // markers go on leaves; the label element wraps exactly one
        if (!(element.getParent() instanceof AsmLabelElement label)
            || element.getPrevSibling() != null
            || !DAsmSymbolIndex.isSymbol(label)) {
            return;
        }
        List<DLanguageFunctionDeclaration> declarations = DAsmUtil.findDDeclarations(label);
        if (declarations.isEmpty()) {
            return;
        }
        result.add(NavigationGutterIconBuilder.create(DLanguage.Icons.FILE)
            .setTargets(declarations)
            .setTooltipText(DLocalize.asmDDeclarationTooltip())
            .createLineMarkerInfo(element));
    }

    @Override
    public Language getLanguage() {
        return AsmLanguage.INSTANCE;
    }
}

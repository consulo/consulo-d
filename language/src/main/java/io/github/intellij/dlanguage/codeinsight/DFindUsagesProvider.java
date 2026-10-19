package io.github.intellij.dlanguage.codeinsight;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.ast.TokenSet;
import consulo.language.cacheBuilder.DefaultWordsScanner;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.ElementDescriptionUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.logging.Logger;
import consulo.usage.UsageViewLongNameLocation;
import consulo.usage.UsageViewNodeTextLocation;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.lexer.DlangLexer;
import io.github.intellij.dlanguage.psi.DLanguageIfCondition;
import io.github.intellij.dlanguage.psi.DTokenSets;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.interfaces.TemplateParameter;
import io.github.intellij.dlanguage.psi.interfaces.VariableDeclaration;
import io.github.intellij.dlanguage.psi.named.*;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class DFindUsagesProvider implements FindUsagesProvider {
    @SuppressWarnings("UnusedDeclaration")
    private final static Logger LOG = Logger.getInstance(DFindUsagesProvider.class);
    // Second parameter is nodes that are PsiNamedElements in practice.


    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new DlangLexer(),
            TokenSet.create(DlangTypes.ID),
            DTokenSets.LINE_COMMENTS, DTokenSets.BLOCK_COMMENTS, DTokenSets.STRING_LITERALS);
    }

    @Override
    public boolean canFindUsagesFor(final PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(final PsiElement psiElement) {
        // TODO: Use HelpID after 13.1.
        return "reference.dialogs.findUsages.other";
    }

    @Override
    public String getType(final PsiElement element) {
//        return ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
//        return "woops";

        if (element instanceof DLanguageFunctionDeclaration) {
            return DLocalize.findUsagesTypeFunction().get();
        } else if (element instanceof DLanguageClassDeclaration) {
            return DLocalize.findUsagesTypeClass().get();
        } else if (element instanceof DLanguageInterfaceDeclaration) {
            return DLocalize.findUsagesTypeInterface().get();
        } else if (element instanceof DLanguageEnumDeclaration) {
            return DLocalize.findUsagesTypeEnum().get();
        } else if (element instanceof TemplateParameter) {
            return DLocalize.findUsagesTypeTemplateParameter().get();
        } else if (element instanceof DLanguageTemplateDeclaration) {
            return DLocalize.findUsagesTypeTemplate().get();
        } else if (element instanceof DLanguageParameter) {
            return DLocalize.findUsagesTypeParameter().get();
        } else if (element instanceof DLanguageUnionDeclaration) {
            return DLocalize.findUsagesTypeUnion().get();
        } else if (element instanceof DLanguageIfCondition) {
            return DLocalize.findUsagesTypeVariable().get();
        } else if (element instanceof DLanguageCatch) {
            return DLocalize.findUsagesTypeCatch().get();
        } else if (element instanceof VariableDeclaration) {
            return DLocalize.findUsagesTypeVariable().get();
        } else {
            return "";
        }

    }

    @Override
    public String getDescriptiveName(final PsiElement element) {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE).get();
//        return "Totally rocks!";
    }

    @Override
    public String getNodeText(final PsiElement element, final boolean useFullName) {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewNodeTextLocation.INSTANCE).get();
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }
}

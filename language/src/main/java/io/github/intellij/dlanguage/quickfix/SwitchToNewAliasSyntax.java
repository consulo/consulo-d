package io.github.intellij.dlanguage.quickfix;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DLanguageType;
import io.github.intellij.dlanguage.psi.impl.DElementFactory;

import java.util.Objects;

/**
 * Created by francis on 11/25/2017.
 */
public class SwitchToNewAliasSyntax extends LocalQuickFixOnPsiElement {

    public SwitchToNewAliasSyntax(DLanguageAliasDeclaration elem) {
        super(elem);
    }

    /*
    * There are various ways in which an alias can be defined. This class need vigorous testing!
    *
    * Check for the following:
    *
    * alias myint = abc.Foo.bar;
    * alias fp = i => 1;
    * alias fp = (i) { return 1; };
    *
    * Also, for situations in which the standard syntax cannot be used (such as an alias for a literal)
    * there is std.meta.Alias that can be used:
    *
    * alias b = Alias!4;
    *
    * not to mention std.meta.AliasSeq:
    *
    * alias TL = AliasSeq!(int, double); // in previous versions of Phobos, this was known as TypeTuple.
    *
    * todo: add testing and fix this code to support various scenarios like "alias int a,b;"
    */
    @Override
    public void invoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {
        DLanguageAliasDeclaration aliasDecl = (DLanguageAliasDeclaration) getStartElement();
        DLanguageType type = aliasDecl.getType();
        int aliasStartIndex = aliasDecl.getTextOffset();
        int restStartIndex = Objects.requireNonNull(aliasDecl.getType()).getTextRange().getEndOffset();
        int restEndIndex = aliasDecl.getTextRange().getEndOffset();
        String restText = aliasDecl.getText().substring(restStartIndex - aliasStartIndex, restEndIndex - aliasStartIndex);
        String newAliasString = "alias " + restText.replace(";", "").strip() + " = " + Objects.requireNonNull(type).getText() + ";";

        DLanguageAliasDeclaration newAliasDeclaration = DElementFactory.createAliasDeclarationFromText(project, newAliasString);
        if (newAliasDeclaration != null) {
            aliasDecl.replace(newAliasDeclaration);
        }
    }

    @Override
    public LocalizeValue getText() {
        return DLocalize.dQuickfixSwitchtonewaliassyntaxText();
    }
}

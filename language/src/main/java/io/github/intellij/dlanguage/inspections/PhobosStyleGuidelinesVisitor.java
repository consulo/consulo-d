package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.psi.DLanguageIdentifierChain;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import io.github.intellij.dlanguage.psi.named.DLanguageInterfaceDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhobosStyleGuidelinesVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;
    private final Pattern myVarFunNameRegex = Pattern.compile("^([\\p{Ll}_][_\\w\\d]*|[\\p{Lu}\\d_]+)$");
    private final Pattern myAggregateNameRegex = Pattern.compile("^\\p{Lu}[\\w\\d]*$");
    private final Pattern myModuleNameRegex = Pattern.compile("^[\\p{Ll}_\\d]+$");

    public PhobosStyleGuidelinesVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    public Pattern getVarFunNameRegex() {
        return myVarFunNameRegex;
    }

    public Pattern getAggregateNameRegex() {
        return myAggregateNameRegex;
    }

    public Pattern getModuleNameRegex() {
        return myModuleNameRegex;
    }

    public void checkName(String type, String name, PsiElement elem, Pattern regex) {
        Matcher matcher = regex.matcher(name);
        if (name.length() > 0 && matcher.find()) {
            myHolder.newProblem(DLocalize.dInspectionsSymbolPhobosstyleguidelinesMessage(type, name))
                .range(elem)
                .create();
        }
    }

    @Override
    public void visitModuleDeclaration(DLanguageModuleDeclaration o) {
        @Nullable DLanguageIdentifierChain identifierChain = o.getIdentifierChain();
        while (identifierChain != null) {
            PsiElement identifier = Objects.requireNonNull(identifierChain.getIdentifier());
            checkName("Module", identifier.getText(), identifier, myModuleNameRegex);
            identifierChain = identifierChain.getIdentifierChain();
        }
    }

    @Override
    public void visitDeclarator(DLanguageIdentifierInitializer o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Variable", Objects.requireNonNull(StringUtil.decapitalize(name)), Objects.requireNonNull(o.getNameIdentifier()), myVarFunNameRegex);
    }

    @Override
    public void visitFunctionDeclaration(DLanguageFunctionDeclaration o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Function", Objects.requireNonNull(StringUtil.decapitalize(name)), Objects.requireNonNull(o.getNameIdentifier()), myVarFunNameRegex);
    }

    @Override
    public void visitClassDeclaration(DLanguageClassDeclaration o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Class", name, o, myAggregateNameRegex);
    }

    @Override
    public void visitInterfaceDeclaration(DLanguageInterfaceDeclaration o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Interface", name, o, myAggregateNameRegex);
    }

    @Override
    public void visitStructDeclaration(DLanguageStructDeclaration o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Struct", name, o, myAggregateNameRegex);
    }

    @Override
    public void visitUnionDeclaration(DLanguageUnionDeclaration o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Union", name, o, myAggregateNameRegex);
    }

    @Override
    public void visitEnumDeclaration(DLanguageEnumDeclaration o) {
        String name = o.getName();
        if (name == null) {
            return;
        }
        checkName("Enum", name, o, myAggregateNameRegex);
    }
}

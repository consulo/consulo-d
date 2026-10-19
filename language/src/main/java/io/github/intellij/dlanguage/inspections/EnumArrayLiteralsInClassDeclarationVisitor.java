package io.github.intellij.dlanguage.inspections;

import consulo.d.localize.DLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.DLanguageInitializer;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.interfaces.DNamedElement;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageStructDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageUnionDeclaration;
import io.github.intellij.dlanguage.quickfix.MakeStaticImmutable;

public class EnumArrayLiteralsInClassDeclarationVisitor extends DlangVisitor {
    private final ProblemsHolder myHolder;

    public EnumArrayLiteralsInClassDeclarationVisitor(ProblemsHolder holder) {
        myHolder = holder;
    }

    public ProblemsHolder getHolder() {
        return myHolder;
    }

    @Override
    public void visitClassDeclaration(DLanguageClassDeclaration o) {
        checkForEnumLiterals(o);
    }

    @Override
    public void visitUnionDeclaration(DLanguageUnionDeclaration o) {
        checkForEnumLiterals(o);
    }

    @Override
    public void visitStructDeclaration(DLanguageStructDeclaration o) {
        checkForEnumLiterals(o);
    }

    public void checkForEnumLiterals(DNamedElement o) {
        for (DLanguageAutoAssignment decl : PsiTreeUtil.findChildrenOfType(o, DLanguageAutoAssignment.class)) {
            if (decl.isEnum()) {
                DLanguageInitializer initializer = decl.getInitializer();
                if (initializer != null && initializer.getKW_VOID() != null) {
                    continue;
                }
                myHolder.newProblem(DLocalize.dInspectionsSymbolEnumarrayliteralsinclassdeclarationMessage())
                    .range(decl)
                    .withFixes(new MakeStaticImmutable(decl))
                    .create();
                // TODO when type deduction becomes a thing use that instead of checking initializers
            }
        }
    }
}

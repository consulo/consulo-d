package io.github.intellij.dlanguage.highlighting.exitpoint;

import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageFunctionLiteralExpression;
import io.github.intellij.dlanguage.psi.DLanguageReturnStatement;
import io.github.intellij.dlanguage.psi.DLanguageThrowExpression;
import io.github.intellij.dlanguage.psi.DlangVisitor;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.function.Consumer;

class ExitPointVisitor extends DlangVisitor {
    private final Consumer<ExitPoint> mySink;

    ExitPointVisitor(Consumer<ExitPoint> sink) {
        mySink = sink;
    }

    @Override
    public void visitPsiElement(PsiElement o) {
        o.acceptChildren(this);
    }

    @Override
    public void visitFunctionDeclaration(DLanguageFunctionDeclaration o) {
    }

    @Override
    public void visitFunctionLiteralExpression(DLanguageFunctionLiteralExpression o) {
    }

    @Override
    public void visitReturnStatement(DLanguageReturnStatement o) {
        mySink.accept(new ExitPoint.Return(o));
    }

    @Override
    public void visitThrowExpression(DLanguageThrowExpression o) {
        mySink.accept(new ExitPoint.Throw(o));
    }
}

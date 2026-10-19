package io.github.intellij.dlanguage.psi.interfaces;

import consulo.language.psi.PsiElement;
import io.github.intellij.dlanguage.psi.DLanguageForeachTypeList;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface Foreach extends PsiElement {

    @Nullable PsiElement getKW_FOREACH();

    @Nullable PsiElement getKW_FOREACH_REVERSE();

    List<Expression> getExpressions();

    @Nullable PsiElement getOP_BRACES_RIGHT();

    @Nullable PsiElement getOP_BRACES_LEFT();

    @Nullable PsiElement getOP_DDOT();

    @Nullable DLanguageForeachTypeList getForeachTypeList();

    @Nullable PsiElement getOP_SCOLON();
}

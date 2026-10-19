package io.github.intellij.dlanguage.sdlang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import io.github.intellij.dlanguage.sdlang.psi.SDLangElement;

public class SDLangElementImpl extends ASTWrapperPsiElement implements SDLangElement {
    public SDLangElementImpl(ASTNode node) {
        super(node);
    }
}

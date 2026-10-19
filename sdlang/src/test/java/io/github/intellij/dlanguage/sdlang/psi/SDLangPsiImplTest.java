package io.github.intellij.dlanguage.sdlang.psi;

import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.CompositeElement;
import consulo.test.junit.impl.extension.ConsuloApplicationLoader;
import io.github.intellij.dlanguage.sdlang.impl.SDLangAttributeImpl;
import io.github.intellij.dlanguage.sdlang.impl.SDLangBinaryLiteralImpl;
import io.github.intellij.dlanguage.sdlang.impl.SDLangBooleanLiteralImpl;
import io.github.intellij.dlanguage.sdlang.impl.SDLangChildrensImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;

/*
 * Doesn't do much in the way of testing but at least ensures that grammarkit codegen worked
 */
@ExtendWith(ConsuloApplicationLoader.class)
public class SDLangPsiImplTest {

    @Test
    public void testAttribute() {
        ASTNode node = new CompositeElement(SDLangElementTypes.ATTRIBUTE);
        SDLangAttribute attribute = new SDLangAttributeImpl(node);
        assertFalse(attribute.isValid());
    }

    @Test
    public void testBinaryLiteral() {
        ASTNode node = new CompositeElement(SDLangElementTypes.BINARY_LITERAL);
        SDLangBinaryLiteral binaryLiteral = new SDLangBinaryLiteralImpl(node);
        assertFalse(binaryLiteral.isValid());
    }

    @Test
    public void testBooleanLiteral() {
        ASTNode node = new CompositeElement(SDLangElementTypes.BOOLEAN_LITERAL);
        SDLangBooleanLiteral booleanLiteral = new SDLangBooleanLiteralImpl(node);
        assertFalse(booleanLiteral.isValid());
    }

    @Test
    public void testChildrens() {
        ASTNode node = new CompositeElement(SDLangElementTypes.CHILDRENS);
        SDLangChildrens childrens = new SDLangChildrensImpl(node);
        assertFalse(childrens.isValid());
    }
}

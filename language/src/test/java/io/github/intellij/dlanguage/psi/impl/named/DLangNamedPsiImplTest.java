package io.github.intellij.dlanguage.psi.impl.named;

import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.CompositeElement;
import consulo.test.junit.impl.extension.ConsuloApplicationLoader;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageDeclaratorIdentifier;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumDeclaration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;

/*
* Doesn't do much in the way of testing but at least ensures that psi-impl has compiled correctly
* with the interfaces made available from psi-api
*/
@ExtendWith(ConsuloApplicationLoader.class)
public class DLangNamedPsiImplTest {

    @Test
    @DisplayName("test Class Declaration")
    public void testClassDeclaration() {
        ASTNode node = new CompositeElement(DlangTypes.CLASS_DECLARATION);
        DLanguageClassDeclaration classDeclaration = new DlangClassDeclarationImpl(node);
        assertFalse(classDeclaration.isValid());
    }

    @Test
    @DisplayName("test Declarator Identifier")
    public void testDeclaratorIdentifier() {
        ASTNode node = new CompositeElement(DlangTypes.DECLARATOR_IDENTIFIER);
        DLanguageDeclaratorIdentifier declaratorIdentifier = new DlangDeclaratorIdentifierImpl(node);
        assertFalse(declaratorIdentifier.isValid());
    }

    @Test
    @DisplayName("test Enum Declaration")
    public void testEnumDeclaration() {
        ASTNode node = new CompositeElement(DlangTypes.ENUM_DECLARATION);
        DLanguageEnumDeclaration enumDeclaration = new DlangEnumDeclarationImpl(node);
        assertFalse(enumDeclaration.isValid());
    }
}

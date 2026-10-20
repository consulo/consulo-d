package io.github.intellij.dlanguage.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.psi.DLanguageAliasDeclaration;
import io.github.intellij.dlanguage.psi.DlangTokenType;
import io.github.intellij.dlanguage.psi.named.DLanguageModuleDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * @author Samael Bate (singingbush)
 * created on 20/02/2022
 */
@ExtendWith(ConsuloProjectLoader.class)
public class DElementFactoryTest {

    @Test
    public void createDLanguageIdentifierFromText(Project project) {
        final PsiElement result = DElementFactory.createDLanguageIdentifierFromText(project, "text");

        assertNotNull(result);
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("text", result.getText());
        assertEquals("text", result.getText());
        assertEquals("ID", debugName(result.getNode().getElementType()));
    }

    @Test
    public void createDLanguageIdentifierFromText_EmptyString(Project project) {
        assertNull(DElementFactory.createDLanguageIdentifierFromText(project, ""));
    }

    @Test
    public void createDLanguageModuleFromText(Project project) {
        final DLanguageModuleDeclaration result = DElementFactory.createDLanguageModuleFromText(project, "mymodule");

        assertNotNull(result);
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("module mymodule;", result.getText());
        assertEquals("MODULE_DECLARATION", debugName(result.getNavigationElement().getNode().getElementType()));
    }

    @Test
    public void createDLanguageModuleFromText_EmptyString(Project project) {
        assertNull(DElementFactory.createDLanguageModuleFromText(project, ""));
    }

    @Test // alias myint = abc.Foo.bar;
    public void createAliasDeclarationFromText_1(Project project) {
        final DLanguageAliasDeclaration result = DElementFactory.createAliasDeclarationFromText(project, "alias myint = abc.Foo.bar;");

        assertNotNull(result);
        assertEquals("alias myint = abc.Foo.bar;", result.getText());
        assertEquals(result.getOriginalElement(), result.getNavigationElement());
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("ALIAS_DECLARATION", debugName(result.getNode().getElementType()));

        final ASTNode[] children = result.getNode().getChildren(TokenSet.ANY);
        assertEquals("alias", debugName(children[0].getElementType()));
        assertEquals("WHITE_SPACE", debugName(children[1].getElementType()));
        assertEquals("ALIAS_INITIALIZER", debugName(children[2].getElementType()));
        assertEquals(";", debugName(children[3].getElementType()));
    }

    @Test // alias fp = i => 1;
    public void createAliasDeclarationFromText_2(Project project) {
        final DLanguageAliasDeclaration result = DElementFactory.createAliasDeclarationFromText(project, "alias fp = i => 1;");

        assertNotNull(result);
        assertEquals("alias fp = i => 1;", result.getText());
        assertEquals(result.getOriginalElement(), result.getNavigationElement());
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("ALIAS_DECLARATION", debugName(result.getNode().getElementType()));

        final ASTNode[] children = result.getNode().getChildren(TokenSet.ANY);
        assertEquals("alias", debugName(children[0].getElementType()));
        assertEquals("WHITE_SPACE", debugName(children[1].getElementType()));
        assertEquals("ALIAS_INITIALIZER", debugName(children[2].getElementType()));
        assertEquals(";", debugName(children[3].getElementType()));
    }

    @Test // alias fp = (i) { return 1; };
    public void createAliasDeclarationFromText_3(Project project) {
        final DLanguageAliasDeclaration result = DElementFactory.createAliasDeclarationFromText(project, "alias fp = (i) { return 1; };");

        assertNotNull(result);
        assertEquals("alias fp = (i) { return 1; };", result.getText());
        assertEquals(result.getOriginalElement(), result.getNavigationElement());
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("ALIAS_DECLARATION", debugName(result.getNode().getElementType()));

        final ASTNode[] children = result.getNode().getChildren(TokenSet.ANY);
        assertEquals("alias", debugName(children[0].getElementType()));
        assertEquals("WHITE_SPACE", debugName(children[1].getElementType()));
        assertEquals("ALIAS_INITIALIZER", debugName(children[2].getElementType()));
        assertEquals(";", debugName(children[3].getElementType()));
    }

    @Test // alias b = Alias!4;
    public void createAliasDeclarationFromText_4(Project project) {
        final DLanguageAliasDeclaration result = DElementFactory.createAliasDeclarationFromText(project, "alias b = Alias!4;");

        assertNotNull(result);
        assertEquals("alias b = Alias!4;", result.getText());
        assertEquals(result.getOriginalElement(), result.getNavigationElement());
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("ALIAS_DECLARATION", debugName(result.getNode().getElementType()));

        final ASTNode[] children = result.getNode().getChildren(TokenSet.ANY);
        assertEquals("alias", debugName(children[0].getElementType()));
        assertEquals("WHITE_SPACE", debugName(children[1].getElementType()));
        assertEquals("ALIAS_INITIALIZER", debugName(children[2].getElementType()));
        assertEquals(";", debugName(children[3].getElementType()));
    }

    @Test // alias TL = AliasSeq!(int, double); // in previous versions of Phobos, this was known as TypeTuple
    public void createAliasDeclarationFromText_5(Project project) {
        final DLanguageAliasDeclaration result = DElementFactory.createAliasDeclarationFromText(project, "alias TL = AliasSeq!(int, double);");

        assertNotNull(result);
        assertEquals("alias TL = AliasSeq!(int, double);", result.getText());
        assertEquals(result.getOriginalElement(), result.getNavigationElement());
        assertEquals(DLanguage.INSTANCE, result.getLanguage());
        assertEquals("ALIAS_DECLARATION", debugName(result.getNode().getElementType()));

        final ASTNode[] children = result.getNode().getChildren(TokenSet.ANY);
        assertEquals("alias", debugName(children[0].getElementType()));
        assertEquals("WHITE_SPACE", debugName(children[1].getElementType()));
        assertEquals("ALIAS_INITIALIZER", debugName(children[2].getElementType()));
        assertEquals(";", debugName(children[3].getElementType()));
    }

    @Test
    public void createAliasDeclarationFromText_EmptyString(Project project) {
        assertNull(DElementFactory.createAliasDeclarationFromText(project, ""));
    }

    /**
     * IntelliJ's {@code IElementType.getDebugName()}, which Consulo does not have: its {@code toString()} is the debug
     * name, except for {@link DlangTokenType}, which prefixes it.
     */
    private static String debugName(IElementType type) {
        return type instanceof DlangTokenType tokenType ? tokenType.getName() : type.toString();
    }
}

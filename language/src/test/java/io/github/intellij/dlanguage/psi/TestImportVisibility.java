package io.github.intellij.dlanguage.psi;

import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.psi.interfaces.DVisibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of upstream {@code TestImportVisibility.kt}. Upstream configures the text in IntelliJ's light fixture and uses its
 * {@code findElementByText}; here the file is created from text and the element is looked up the same way.
 */
@ExtendWith(ConsuloProjectLoader.class)
public class TestImportVisibility {

    @Test
    public void testSimpleImport(Project project) {
        doTest(project, DVisibility.Private.INSTANCE, "import myimport;");
    }

    @Test
    public void testPublicImport(Project project) {
        doTest(project, DVisibility.Public.INSTANCE, "public import myimport;");
    }

    @Test
    public void testImportInPublicBlock(Project project) {
        doTest(project, DVisibility.Public.INSTANCE, "public: import myimport;");
    }

    @Test
    public void testImportInPublicBlockWithOtherDeclaration(Project project) {
        doTest(project, DVisibility.Public.INSTANCE, "public: struct A {} import myimport;");
    }

    @Test
    public void testImportInPublicStruct(Project project) {
        doTest(project, DVisibility.Private.INSTANCE, "public: struct A { import myimport; }");
    }

    @Test
    public void testImportInPrivateAfterPublic(Project project) {
        doTest(project, DVisibility.Private.INSTANCE, "public:private: import myimport;");
    }

    @Test
    public void testImportPublicBlock(Project project) {
        doTest(project, DVisibility.Private.INSTANCE, "public{} import myimport;");
    }

    @Test
    public void testImportInPublicConstructor(Project project) {
        doTest(project, DVisibility.Private.INSTANCE, "class A { public this() { import myimport; } }");
    }

    @Test
    public void testImportInConditionalDeclaration(Project project) {
        doTest(project, DVisibility.Public.INSTANCE, "public: version(Foo) { import myimport; }");
    }

    @Test
    public void testImportInConditionalDeclarationWithDoubleAttribute(Project project) {
        doTest(project, DVisibility.Private.INSTANCE, "public:private: version(Foo) { import myimport; }");
    }

    private void doTest(Project project, DVisibility expectedVisibility, String text) {
        checkVisibility(expectedVisibility, parseImportDeclaration(project, text));
    }

    private DLanguageImportDeclaration parseImportDeclaration(Project project, String text) {
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText("aaa.d", DlangFileType.INSTANCE, text);
        int pos = text.indexOf("myimport");
        assertTrue(pos >= 0, "myimport");
        DLanguageImportDeclaration element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageImportDeclaration.class);
        assertNotNull(element);
        return element;
    }

    private void checkVisibility(DVisibility expectedVisibility, DLanguageImportDeclaration expr) {
        assertNotNull(expr);
        assertEquals(expectedVisibility, expr.visibility());
    }
}

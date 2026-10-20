package io.github.intellij.dlanguage.types;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageIdentifierInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessProjectExtension.class)
public class TypedDefinedAssignmentTypeTest {

    @Test
    public void testEnsureNonAutoVariableDeclarationTypedCorrectly(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "bool", "bool expr;");
        doTest(fixture, "byte", "byte expr;");
        doTest(fixture, "ubyte", "ubyte expr;");
        doTest(fixture, "short", "short expr;");
        doTest(fixture, "ushort", "ushort expr;");
        doTest(fixture, "int", "int expr;");
        doTest(fixture, "uint", "uint expr;");
        doTest(fixture, "long", "long expr;");
        doTest(fixture, "ulong", "ulong expr;");
        doTest(fixture, "float", "float expr;");
        doTest(fixture, "double", "double expr;");
        doTest(fixture, "char", "char expr;");
        doTest(fixture, "wchar", "wchar expr;");
        doTest(fixture, "dchar", "dchar expr;");
    }

    @Test
    public void testSimpleCustomTypeInstance(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "Foo", """
            class Foo {}

            Foo expr;""");
        doTest(fixture, "Foo", """
            interface Foo {}

            Foo expr;""");
        doTest(fixture, "Foo", """
            struct Foo {}

            Foo expr;""");
        doTest(fixture, "Foo", """
            enum Foo { E }

            Foo expr;""");
        doTest(fixture, "Foo", """
            union Foo {}

            Foo expr;""");
    }

    @Test
    public void testArrayTypeInstance(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "char[]", "char[] expr;");
        doTest(fixture, "char[]", "char[12] expr;"); // TODO should be char[12] but it is currently unsupported
        doTest(fixture, "int[int]", "int[int] expr;");
    }

    /**
     * Take a file text and the expected type string representation and ensure that the expression named (expr) has the corresponding type
     */
    private void doTest(CodeInsightTestFixture fixture, String expectedType, String text) throws Exception {
        PsiFile file = fixture.configureByText("aaa.d", text);
        try {
            ReadAction.run(() -> checkTypes(expectedType, parseVariableDeclaration(file, text)));
        }
        finally {
            deleteFile(file);
        }
    }

    private DLanguageIdentifierInitializer parseVariableDeclaration(PsiFile file, String text) {
        int pos = text.indexOf("expr");
        assertTrue(pos >= 0, "expr");
        DLanguageIdentifierInitializer element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageIdentifierInitializer.class);
        assertNotNull(element);
        return element;
    }

    private void checkTypes(String expectedType, DLanguageIdentifierInitializer expr) {
        assertNotNull(expr);
        assertEquals(expectedType, String.valueOf(expr.getDType()));
    }

    /**
     * IntelliJ's fixture replaces the file on every {@code configureByText}; in consulo-it the PSI of a file written
     * again keeps its previous text, so each check removes the file it configured.
     */
    private void deleteFile(PsiFile file) throws Exception {
        WriteAction.run(() -> file.getVirtualFile().delete(this));
    }
}

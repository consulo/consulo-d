package io.github.intellij.dlanguage.types;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageForeachType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessProjectExtension.class)
public class ForeachVariableTypeTest {

    @Test
    public void testForeachVariableFromArray(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "void foo() { int[] arr; foreach(expr; arr) { } }");
        doTest(fixture, "int", "void foo() { foreach(expr; [1, 2, 3]) { } }");
        doTest(fixture, "float", "void foo() { foreach(expr; [1f, 2f, 3f]) { } }");
        doTest(fixture, "double", "void foo() { foreach(expr; [1, 2., 3]) { } }");
        doTest(fixture, "char", "void foo() { foreach(expr; \"test\") { } }");
        doTest(fixture, "int", "void foo() { foreach(expr; 1 .. 3) { } }");
        doTest(fixture, "long", "void foo() { foreach(expr; 1 .. 3L) { } }");
        doTest(fixture, "long", "void foo() { foreach(expr; 1L .. 3) { } }");
        doTest(fixture, "long", "void foo() { foreach(idx, expr; [1, 2, 3L]) { } }");
    }

    @Test
    public void testForeachVariableIndexFromArray(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "ushort", "void foo() { foreach(expr, a; [1, 2, 3L]) { } }");
        doTest(fixture, "int", "void foo() { int[] arr; foreach(expr, a; arr) { } }"); // TODO size_t
    }

    @Test
    public void testForeachVariableFromAssociativeArray(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "void foo() { int[char] arr; foreach(expr; arr) { } }");
        doTest(fixture, "char", "void foo() { int[char] arr; foreach(expr, a; arr) { } }");
        doTest(fixture, "int", "void foo() { int[char] arr; foreach(a, expr; arr) { } }");
        doTest(fixture, "int", "void foo() { int[char] arr; foreach(a, ref expr; arr) { } }");
        doTest(fixture, "int", "void foo() { foreach(a, expr; [\"key\": 1]) { } }");
        doTest(fixture, "char[]", "void foo() { foreach(expr, a; [\"key\": 1]) { } }");
    }

    @Test
    public void testStaticForeachVariableFromArray(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "static foreach(expr; [1, 2, 3]) { }");
    }

    private void doTest(CodeInsightTestFixture fixture, String expectedType, String text) throws Exception {
        PsiFile file = fixture.configureByText("aaa.d", text);
        try {
            ReadAction.run(() -> checkTypes(expectedType, parseForeach(file, text)));
        }
        finally {
            deleteFile(file);
        }
    }

    private DLanguageForeachType parseForeach(PsiFile file, String text) {
        int pos = text.indexOf("expr");
        assertTrue(pos >= 0, "expr");
        DLanguageForeachType element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageForeachType.class);
        assertNotNull(element);
        return element;
    }

    private void checkTypes(String expectedType, DLanguageForeachType expr) {
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

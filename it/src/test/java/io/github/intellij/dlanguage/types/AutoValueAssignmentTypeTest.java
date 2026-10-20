package io.github.intellij.dlanguage.types;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageAutoAssignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessProjectExtension.class)
public class AutoValueAssignmentTypeTest {

    @Test
    public void testAutoExpressionLiteralExpressionAssignment(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "auto expr = 1;");
        doTest(fixture, "float", "auto expr = 1f;");
        doTest(fixture, "float", "auto expr = 1F;");
        doTest(fixture, "long", "auto expr = 1L;");
        doTest(fixture, "long", "auto expr = 0b1L;");
        doTest(fixture, "long", "auto expr = 0xFL;");
        doTest(fixture, "ulong", "auto expr = 1UL;");
        doTest(fixture, "double", "auto expr = 1.0;");
        doTest(fixture, "double", "auto expr = 1.;");
        doTest(fixture, "float", "auto expr = 1.0f;");
        doTest(fixture, "bool", "auto expr = true;");
        doTest(fixture, "bool", "auto expr = false;");
        doTest(fixture, "char", "auto expr = 'a';");

        // Check against limit provided in the lexer spec page
        doTest(fixture, "int", "auto expr = 0;");
        doTest(fixture, "int", "auto expr = 2_147_483_647;");
        doTest(fixture, "long", "auto expr = 2_147_483_648;");
        doTest(fixture, "long", "auto expr = 9_223_372_036_854_775_807;");
        doTest(fixture, "ulong", "auto expr = 9_223_372_036_854_775_808;");
        doTest(fixture, "ulong", "auto expr = 18_446_744_073_709_551_615;");

        doTest(fixture, "long", "auto expr = 0L;");
        doTest(fixture, "long", "auto expr = 9_223_372_036_854_775_807L;");
        doTest(fixture, "uint", "auto expr = 0U;");
        doTest(fixture, "uint", "auto expr = 4_294_967_295U;");
        doTest(fixture, "ulong", "auto expr = 4_294_967_296U;");
        doTest(fixture, "ulong", "auto expr = 18_446_744_073_709_551_615U;");
        doTest(fixture, "ulong", "auto expr = 0UL;");
        doTest(fixture, "ulong", "auto expr = 18_446_744_073_709_551_615UL;");

        doTest(fixture, "int", "auto expr = 0x0;");
        doTest(fixture, "int", "auto expr = 0x7FFF_FFFF;");
        doTest(fixture, "uint", "auto expr = 0x8000_0000;");
        doTest(fixture, "uint", "auto expr = 0xFFFF_FFFF;");
        doTest(fixture, "long", "auto expr = 0x1_0000_0000;");
        doTest(fixture, "long", "auto expr = 0x7FFF_FFFF_FFFF_FFFF;");
        doTest(fixture, "ulong", "auto expr = 0x8000_0000_0000_0000;");
        doTest(fixture, "ulong", "auto expr = 0xFFFF_FFFF_FFFF_FFFF;");

        doTest(fixture, "long", "auto expr = 0x0L;");
        doTest(fixture, "long", "auto expr = 0x7FFF_FFFF_FFFF_FFFFL;");
        doTest(fixture, "ulong", "auto expr = 0x8000_0000_0000_0000L;");
        doTest(fixture, "ulong", "auto expr = 0xFFFF_FFFF_FFFF_FFFFL;");
        doTest(fixture, "uint", "auto expr = 0x0U;");
        doTest(fixture, "uint", "auto expr = 0xFFFF_FFFFU;");
        doTest(fixture, "ulong", "auto expr = 0x1_0000_0000U;");
        doTest(fixture, "ulong", "auto expr = 0xFFFF_FFFF_FFFF_FFFFU;");
        doTest(fixture, "ulong", "auto expr = 0x0UL;");
        doTest(fixture, "ulong", "auto expr = 0xFFFF_FFFF_FFFF_FFFFUL;");

        doTest(fixture, "float", "auto expr = -1.175494351e-38F;");
        doTest(fixture, "double", "auto expr = 0xAp0;");
        doTest(fixture, "double", "auto expr = 0x1.FFFFFFFFFFFFFp1023;");
        doTest(fixture, "double", "auto expr = 0x1p-52;");
    }

    @Test
    public void testAutoExpressionArrayAssignment(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "char", "char[] x; auto expr = x[3];");
        doTest(fixture, "char[]", "char* x; auto expr = x[0 .. 7];");
    }

    @Test
    public void testFunctionCall(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "void[]", "void[] a() {} auto expr = a();");
    }

    @Test
    public void testAutoExpressionPropertiesCallAssignment(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "int[] x; auto expr = x.length;");
        doTest(fixture, "void*", "void[] x; auto expr = x.ptr;");
    }

    /**
     * Take a file text and the expected type string representation and ensure that the expression named (expr) has the corresponding type
     */
    private void doTest(CodeInsightTestFixture fixture, String expectedType, String text) throws Exception {
        PsiFile file = fixture.configureByText("aaa.d", text);
        try {
            ReadAction.run(() -> checkTypes(expectedType, parseAutoDeclaration(file, text)));
        }
        finally {
            deleteFile(file);
        }
    }

    private DLanguageAutoAssignment parseAutoDeclaration(PsiFile file, String text) {
        int pos = text.indexOf("expr");
        assertTrue(pos >= 0, "expr");
        DLanguageAutoAssignment element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageAutoAssignment.class);
        assertNotNull(element);
        return element;
    }

    private void checkTypes(String expectedType, DLanguageAutoAssignment expr) {
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

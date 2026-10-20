package io.github.intellij.dlanguage.types;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageEnumMember;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessProjectExtension.class)
public class EnumTypeTest {

    @Test
    public void testEnumMemberOfEnumType(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "x", "enum x { MEMBER }");
        doTest(fixture, "x", "enum x { foo = 3L, MEMBER }");
        doTest(fixture, "x", "enum x : int { MEMBER }");
        doTest(fixture, "x", "enum x : short { MEMBER }");
        doTest(fixture, "x", "enum x { short MEMBER }");
    }

    // Need to introduce another method or whatever to allow to gen the "native" type of the member
    // TODO need to add a method in enum to get the native type (not the enum type but the "final" type)
    /*@Test
    fun testEnumMemberOfEnumType() {
        doTestNativeType("int", """enum x { MEMBER }""")
        doTestNativeType("long", """enum x { foo = 3L, MEMBER }""")
        doTestNativeType("int", """enum x : int { MEMBER }""")
        doTestNativeType("short", """enum x : short { MEMBER }""")
        doTestNativeType("int", """enum x { short MEMBER }""") // named enum cannot have type per member
    }*/

    @Test
    public void testEnumMemberOfAnonymousEnumType(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "enum { MEMBER }");
        doTest(fixture, "short", "enum : short { MEMBER }");
        doTest(fixture, "long", "enum { long MEMBER = 3 }");
        doTest(fixture, "long", "enum { MEMBER = 3L }");
        doTest(fixture, "long", "enum { MEMBER = B, B = C, C = 3L }");
        doTest(fixture, "short", "enum { long bar = 1, short Foo = 2, MEMBER }");
        doTest(fixture, "long", "enum : long { short MEMBER = 3 }"); // anonymous enum and member type cannot have a type at the same time
    }

    /**
     * Take a file text and the expected type string representation and ensure that the expression named (expr) has the corresponding type
     */
    private void doTest(CodeInsightTestFixture fixture, String expectedType, String text) throws Exception {
        PsiFile file = fixture.configureByText("aaa.d", text);
        try {
            ReadAction.run(() -> checkTypes(expectedType, parseEnumDeclaration(file, text)));
        }
        finally {
            deleteFile(file);
        }
    }

    private DLanguageEnumMember parseEnumDeclaration(PsiFile file, String text) {
        int pos = text.indexOf("MEMBER");
        assertTrue(pos >= 0, "MEMBER");
        DLanguageEnumMember element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageEnumMember.class);
        assertNotNull(element);
        return element;
    }

    private void checkTypes(String expectedType, DLanguageEnumMember expr) {
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

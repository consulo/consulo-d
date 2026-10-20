package io.github.intellij.dlanguage.types;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessProjectExtension.class)
public class ParameterTypeTest {


    @Test
    public void testSimpleParameterInstance(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture,
            "Foo", """
                class Foo {}

                void foo(Foo expr) {}"""
        );
        doTest(fixture,
            "Foo", """
                interface Foo {}

                void foo(Foo expr) {}"""
        );
        doTest(fixture,
            "Foo", """
                struct Foo {}

                void foo(Foo expr) {}"""
        );
        doTest(fixture,
            "Foo", """
                enum Foo { E }

                void foo(Foo expr) {}"""
        );
        doTest(fixture,
            "Foo", """
                union Foo {}

                void foo(Foo expr) {}"""
        );
    }

    @Test
    public void testAliasedTypeParameterInstance(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "alias Foo = int", """
            alias Foo = int;

            void foo(Foo expr) {}""");
    }

    /**
     * Take a file text and the expected type string representation and ensure that the expression named (expr) has the corresponding type
     */
    private void doTest(CodeInsightTestFixture fixture, String expectedType, String text) throws Exception {
        PsiFile file = fixture.configureByText("aaa.d", text);
        try {
            ReadAction.run(() -> checkTypes(expectedType, parseParameter(file, text)));
        }
        finally {
            deleteFile(file);
        }
    }

    private DLanguageParameter parseParameter(PsiFile file, String text) {
        int pos = text.indexOf("expr");
        assertTrue(pos >= 0, "expr");
        DLanguageParameter element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageParameter.class);
        assertNotNull(element);
        return element;
    }

    private void checkTypes(String expectedType, DLanguageParameter expr) {
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

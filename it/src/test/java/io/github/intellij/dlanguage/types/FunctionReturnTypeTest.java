package io.github.intellij.dlanguage.types;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(HeadlessProjectExtension.class)
public class FunctionReturnTypeTest {
    @Test
    public void testDefinedReturnedTypeFunction(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "int", "int func() {}");
        doTest(fixture, "void[]", "void[] func() {}");
    }

    /**
     * Take a file text and the expected type string representation and ensure that the function named (func) has the corresponding type
     */
    private void doTest(CodeInsightTestFixture fixture, String expectedType, String text) throws Exception {
        PsiFile file = fixture.configureByText("aaa.d", text);
        try {
            ReadAction.run(() -> checkTypes(expectedType, parseFunctionDeclaration(file, text)));
        }
        finally {
            deleteFile(file);
        }
    }

    private DLanguageFunctionDeclaration parseFunctionDeclaration(PsiFile file, String text) {
        int pos = text.indexOf("func");
        assertTrue(pos >= 0, "func");
        DLanguageFunctionDeclaration element = PsiTreeUtil.getParentOfType(file.findElementAt(pos), DLanguageFunctionDeclaration.class);
        assertNotNull(element);
        return element;
    }

    private void checkTypes(String expectedType, DLanguageFunctionDeclaration func) {
        assertNotNull(func);
        assertEquals(expectedType, String.valueOf(func.getReturnDType()));
    }

    /**
     * IntelliJ's fixture replaces the file on every {@code configureByText}; in consulo-it the PSI of a file written
     * again keeps its previous text, so each check removes the file it configured.
     */
    private void deleteFile(PsiFile file) throws Exception {
        WriteAction.run(() -> file.getVirtualFile().delete(this));
    }
}

package io.github.intellij.dlanguage.codeinsight;

import consulo.application.ReadAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code DFindUsagesTest.java}. Upstream's {@code myFixture.testFindUsages} finds the element at the
 * caret and collects its usages; here the reference at the caret is resolved and its usages are searched with
 * {@link ReferencesSearch}, which reads the word index built with {@link DFindUsagesProvider#getWordsScanner()}.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DFindUsagesTest {
    @Test
    @Disabled("consulo-it binds no PsiSearchHelper (Consulo's binding lives in ide-impl), so ReferencesSearch cannot run")
    public void testFunctionUsagesInSingleFile(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, "FunctionUsagesInSingleFile", 1);
    }

//    @Test public void testFunctionUsagesInMultipleFiles00001() { doTestModule(3, "FunctionUsagesInSingleFile00001.hs");}
//    @Test public void testFunctionUsagesInSingleFile00002()    { doTestModule(2); }

    private void doTest(CodeInsightTestFixture fixture, String testName, int expectedResult) throws Exception {
        String fileName = testName + ".d";
        fixture.configureByText(fileName, load("/gold/codeinsight/" + fileName));
        PsiElement target = ReadAction.compute(() -> fixture.getReferenceAtCaretPositionWithAssertion().resolve());
        assertNotNull(target, "the reference at the caret does not resolve");
        Collection<PsiReference> usages = ReadAction.compute(() -> ReferencesSearch.search(target).findAll());
        assertEquals(expectedResult, usages.size());
    }

    private String load(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

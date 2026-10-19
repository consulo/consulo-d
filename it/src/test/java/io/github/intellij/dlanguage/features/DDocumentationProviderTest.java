package io.github.intellij.dlanguage.features;

import consulo.application.ReadAction;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorFactory;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.language.editor.impl.internal.documentation.DocumentationElementSource;
import consulo.language.editor.impl.internal.documentation.DocumentationTargetFinder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of upstream {@code DDocumentationProviderTest.kt}, a {@code LightPlatformCodeInsightFixture4TestCase}.
 * Consulo has no {@code DocumentationManager.findTargetElementAndContext}: the target and the context element come from
 * {@code DocumentationTargetFinder}, the platform code behind the documentation popup, on a headless editor, at the
 * offset where upstream puts the caret.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DDocumentationProviderTest {

    private final DDocumentationProvider provider = new DDocumentationProvider();

    private record ElementAndContext(PsiElement first, PsiElement second) {
    }

    @Test
    public void testGetUrlForHandlesSingleImport(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", "import std.typecons;");

        ElementAndContext elementAndContext = findTargetElementAndContext(fixture, 13);

        List<String> result = ReadAction.compute(() -> provider.getUrlFor(elementAndContext.first(), elementAndContext.second()));
        assertEquals("https://dlang.org/phobos/std_typecons.html", result.get(0));
    }

    @Test
    public void testGetUrlForHandlesNullAndNonImportStatement(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", "class User { int id; string name;}");

        ElementAndContext elementAndContext = findTargetElementAndContext(fixture, 17);

        ReadAction.run(() -> {
            assertTrue(provider.getUrlFor(null, null).isEmpty(), "Should return empty list rather than throwing exception");
            assertTrue(provider.getUrlFor(elementAndContext.first(), null).isEmpty(), "Should return empty list rather than throwing exception");
            assertTrue(provider.getUrlFor(null, elementAndContext.second()).isEmpty(), "Should return empty list rather than throwing exception");
            assertTrue(provider.getUrlFor(elementAndContext.first(), elementAndContext.second()).isEmpty(), "Should return empty list rather than throwing exception");
        });
    }

    @Test
    public void testGenerateDocHandlesNull() {
        String text = provider.generateDoc(null, null);
        assertNull(text, "Should return null rather than throwing exception");
    }

    @Test
    public void testGenerateDocForPublicClassVariable(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", "class User { int id; string name;}");

        // class User { int id; string name;}
        //                  ^
        PsiElement docElement = findTargetElement(fixture, 17);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);
        assertTrue(text.contains("int"));
        assertTrue(text.contains("id"));
    }

    @Test
    public void testGenerateDocForPrivateClassVariable(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", "class User { int id; private string name;}");

        // class User { int id; private string name;}
        //                                      ^
        PsiElement docElement = findTargetElement(fixture, 37);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);
        //assertTrue(text.contains("private")) XXX it’s private, so we can display it
        assertTrue(text.contains("string"));
        assertTrue(text.contains("name"));
    }

    @Test
    public void testGenerateDocForGlobalInt(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("int.d", "int x = 0;");

        // int x = 0;
        //     ^
        PsiElement docElement = findTargetElement(fixture, 4);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);
        assertTrue(text.contains("int"));
        assertTrue(text.contains("x"));
    }

    /*
     * Run a test that uses the "gold/documentation/example.d" file
     */
    @Test
    public void testGenerateDocForFileBasedExample(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", loadTestData("example.d"));

        DLanguageFunctionDeclaration doSomethingMethod = findElementByText(fixture, "doSomething", DLanguageFunctionDeclaration.class);

        // put the caret on the doSomething() function in the source file
        int offset = ReadAction.compute(() -> doSomethingMethod.getIdentifier().getTextRange().getStartOffset());
        PsiElement docElement = findTargetElement(fixture, offset);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);
        //assertTrue(text!!.contains("static"))
        assertTrue(text.contains("void"));
        assertTrue(text.contains("doSomething"));
        assertTrue(text.contains("()"));
        assertTrue(text.contains("This is the method documentation."));
    }

    @Test
    public void testGenerateDocForClassBasedExample(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", loadTestData("example.d"));

        DLanguageClassDeclaration myCodeClass = findElementByText(fixture, "MyCode", DLanguageClassDeclaration.class);

        // put the caret on the doSomething() function in the source file
        int offset = ReadAction.compute(() -> myCodeClass.getIdentifier().getTextRange().getStartOffset());
        PsiElement docElement = findTargetElement(fixture, offset);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);

        assertTrue(text.contains("This is the CLASS documentation"));
    }


    @Test
    public void testGenerateDocWithEmptyCodeSnippet(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", """
            /**
             Contains empty code snippet
             ~~~ ~~~*/
             class I {}""");

        DLanguageClassDeclaration myCodeClass = findElementByText(fixture, "I", DLanguageClassDeclaration.class);

        // put the caret on the doSomething() function in the source file
        int offset = ReadAction.compute(() -> myCodeClass.getIdentifier().getTextRange().getStartOffset());
        PsiElement docElement = findTargetElement(fixture, offset);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);

        assertTrue(text.contains("Contains empty code snippet"));
    }

    @Test
    public void testGenerateDocWithEmptyDocComment(CodeInsightTestFixture fixture) throws Exception {
        fixture.configureByText("example.d", """
            ///
            class I {}""");

        DLanguageClassDeclaration myCodeClass = findElementByText(fixture, "I", DLanguageClassDeclaration.class);

        // put the caret on the declaration in the source file
        int offset = ReadAction.compute(() -> myCodeClass.getIdentifier().getTextRange().getStartOffset());
        PsiElement docElement = findTargetElement(fixture, offset);
        String text = ReadAction.compute(() -> provider.generateDoc(docElement, null));
        assertNotNull(text);
        assertFalse(text.contains("<div class='content'>"));
    }

    /**
     * {@code DocumentationManager.findTargetElementAndContext(editor, offset, file)}.
     */
    private static ElementAndContext findTargetElementAndContext(CodeInsightTestFixture fixture, int offset) {
        DocumentationElementSource source = withEditor(fixture, editor -> ReadAction.compute(
            () -> DocumentationTargetFinder.findAtOffset(fixture.getProject(), editor, fixture.getFile(), offset, null)));
        assertNotNull(source);
        return ReadAction.compute(() -> {
            PsiElement element = source.element().getElement();
            PsiElement context = source.originalElement() == null ? null : source.originalElement().getElement();
            assertNotNull(element);
            assertNotNull(context);
            return new ElementAndContext(element, context);
        });
    }

    /**
     * {@code DocumentationManager.findTargetElement(editor, file)} with the caret at {@code offset}.
     */
    private static PsiElement findTargetElement(CodeInsightTestFixture fixture, int offset) {
        return withEditor(fixture, editor -> ReadAction.compute(() -> {
            PsiFile file = fixture.getFile();
            return DocumentationTargetFinder.findTargetElement(fixture.getProject(), editor, offset, file, file.findElementAt(offset));
        }));
    }

    private static <T> T withEditor(CodeInsightTestFixture fixture, Function<Editor, T> action) {
        Project project = fixture.getProject();
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = project.getUIAccess().giveAsync(() -> editorFactory.createEditor(fixture.getDocument(), project)).join();
        try {
            return action.apply(editor);
        }
        finally {
            project.getUIAccess().giveAsync(() -> {
                editorFactory.releaseEditor(editor);
                return null;
            }).join();
        }
    }

    /**
     * {@code myFixture.findElementByText(text, elementClass)}.
     */
    private static <T extends PsiElement> T findElementByText(CodeInsightTestFixture fixture, String text, Class<T> elementClass) {
        return ReadAction.compute(() -> {
            PsiFile file = fixture.getFile();
            int offset = file.getText().indexOf(text);
            T element = PsiTreeUtil.getParentOfType(file.findElementAt(offset), elementClass, true);
            assertNotNull(element);
            return element;
        });
    }

    private String loadTestData(String name) throws Exception {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("gold/documentation/" + name)) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

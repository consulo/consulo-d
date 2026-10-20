package io.github.intellij.dlanguage.resolve;

import consulo.application.ReadAction;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.it.CodeInsightTestFixture;
import consulo.language.impl.internal.psi.PsiManagerEx;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.GlobalSearchScope;
import io.github.intellij.dlanguage.psi.named.DLanguageConstructor;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Port of upstream {@code DResolveTestCase.kt}. Upstream's {@code myFixture.configureByText} added every file to the
 * project; here the files go to the source root with {@link CodeInsightTestFixture#addFileToProject}.
 */
public abstract class DResolveTestCase {
    private @Nullable PsiReference referencedElement;
    private @Nullable PsiElement resolvedElement;
    private String testName = "";
    private int stubOnlyResolveCount;

    @BeforeEach
    void rememberTestName(TestInfo info) {
        String name = info.getTestMethod().map(method -> method.getName()).orElse("");
        testName = getTestName(name);
    }

    /**
     * Upstream's {@code getTestName(true)}: the method name without the {@code test} prefix, first letter lower case.
     */
    private static String getTestName(String methodName) {
        String name = methodName.startsWith("test") ? methodName.substring("test".length()) : methodName;
        return name.isEmpty() ? name : Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    protected String getTestDataPath() {
        return "gold/resolve/" + testName;
    }

    private List<Path> getTestDataFiles() {
        URL url = DResolveTestCase.class.getClassLoader().getResource(getTestDataPath());
        assertNotNull(url, getTestDataPath());
        try (Stream<Path> files = Files.list(Path.of(url.toURI()))) {
            return files.sorted().toList();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private void prepareFilesFindReferences(CodeInsightTestFixture fixture, List<Path> files) throws Exception {
        for (Path file : files) {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            text = convertLineSeparators(text);
            prepareFileFindReferences(fixture, "<ref>", "<resolved>", text, String.valueOf(file.getFileName()));
        }
    }

    protected void prepareFileFindReferences(CodeInsightTestFixture fixture,
                                             String refName,
                                             String resolvedName,
                                             String fileText,
                                             String fileName) throws Exception {
        String text = fileText;
        int refIndexBefore = text.indexOf(refName);
        int resolvedIndexBefore = text.indexOf(resolvedName);
        int referencedOffset;
        int resolvedOffset;
        if (refIndexBefore >= 0 && resolvedIndexBefore >= 0 && refIndexBefore < resolvedIndexBefore) {
            referencedOffset = text.indexOf(refName);
            text = text.replace(refName, "");
            resolvedOffset = text.indexOf(resolvedName);
            text = text.replace(resolvedName, "");
        }
        else {
            resolvedOffset = text.indexOf(resolvedName);
            text = text.replace(resolvedName, "");
            referencedOffset = text.indexOf(refName);
            text = text.replace(refName, "");
        }
        PsiFile psiFile = fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/" + fileName, text);
        if (referencedOffset != -1) {
            PsiReference reference = ReadAction.compute(() -> psiFile.findReferenceAt(referencedOffset));
            assertNotNull(reference, "no reference at " + referencedOffset + " in " + fileName);
            referencedElement = reference;
        }
        if (resolvedOffset != -1) {
            PsiElement resolved = ReadAction.compute(() -> {
                PsiElement leaf = psiFile.findElementAt(resolvedOffset);
                return leaf != null ? leaf.getParent() : null;
            });
            assertNotNull(resolved, "no element at " + resolvedOffset + " in " + fileName);
            resolvedElement = resolved;
        }
    }

    protected void doCheck(boolean succeed) {
        doCheck(succeed, "<ref>", "<resolved>");
    }

    protected void doCheck(boolean succeed, String refName, String resolvedName) {
        PsiReference reference = referencedElement;
        PsiElement resolved = resolvedElement;
        if (succeed && reference == null) {
            fail("Reference not found, ensure `" + refName + "` is in the file.");
        }
        if (succeed && resolved == null) {
            fail("Resolved not found, ensure `" + resolvedName + "` is in the file.");
        }
        if (succeed) {
            PsiElement element = ReadAction.compute(reference::resolve);
            if (resolved instanceof DLanguageConstructor) {
                assertEquals(resolved, element, "Could not resolve expected reference.");
            }
            else if (testName.equals("scopedImportsMembers")) {
                assertNotNull(element, "Could not resolve expected reference.");
                assertEquals("struct_member", ReadAction.compute(() -> ((DLanguageFunctionDeclaration) element).getName()),
                    "Could not resolve expected reference.");
            }
            else {
                assertNotNull(element, "Could not resolve expected reference.");
                assertEquals(resolved, element, "Could not resolve expected reference.");
            }
        }
        else {
            assertNotNull(reference, "Reference not found, ensure `" + refName + "` is in the file.");
            PsiElement element = ReadAction.compute(reference::resolve);
            if (resolved == null) {
                assertNull(element, "The reference should not resolve to anything but a reference was found");
            }
            else {
                assertNotEquals(resolved, element, "Resolved unexpected reference.");
            }
        }
    }

    protected void doTest(CodeInsightTestFixture fixture) throws Exception {
        doTest(fixture, true);
    }

    protected void doTest(CodeInsightTestFixture fixture, boolean succeed) throws Exception {
        prepareFilesFindReferences(fixture, getTestDataFiles());
        doCheck(succeed);
    }

    protected void doCheckByText1(CodeInsightTestFixture fixture, String mainFileContent) throws Exception {
        doCheckByText1(fixture, mainFileContent, true);
    }

    protected void doCheckByText1(CodeInsightTestFixture fixture, String mainFileContent, boolean succeed) throws Exception {
        String referenceIndicator = "/*<ref>*/";
        String resolvedIndicator = "/*<resolved>*/";
        prepareFileFindReferences(fixture, referenceIndicator, resolvedIndicator, mainFileContent, "main.d");
        doCheck(succeed, referenceIndicator, resolvedIndicator);
    }

    protected void doCheckByText2(CodeInsightTestFixture fixture, String mainFileContent, String file2) throws Exception {
        doCheckByText2(fixture, mainFileContent, file2, true);
    }

    protected void doCheckByText2(CodeInsightTestFixture fixture,
                                  String mainFileContent,
                                  String file2,
                                  boolean succeed) throws Exception {
        String referenceIndicator = "/*<ref>*/";
        String resolvedIndicator = "/*<resolved>*/";
        prepareFileFindReferences(fixture, referenceIndicator, resolvedIndicator, mainFileContent, "main.d");
        prepareFileFindReferences(fixture, referenceIndicator, resolvedIndicator, file2, "file2.d");
        doCheck(succeed);
    }

    /**
     * Consulo: upstream installed the AST loading assertion after the resolve, so it checked nothing; here it is
     * installed before the resolve and rejects loading the AST of every file but {@code main.d}.
     */
    protected void doTestStubOnlyResolve(CodeInsightTestFixture fixture,
                                         String mainFileContent,
                                         @Nullable String resolvedFileName) throws Exception {
        String referenceIndicator = "/*<ref>*/";
        String mainFileText = convertLineSeparators(mainFileContent);
        int referencedOffset = mainFileText.indexOf(referenceIndicator);
        assertTrue(referencedOffset > 0, "Test is not properly configured, need " + referenceIndicator + " to be defined");
        mainFileText = mainFileText.replace(referenceIndicator, "");
        // Consulo: rewriting an open file races with its reparse, so every check gets its own main.d
        PsiFile psiMainFile = fixture.configureByText("check" + stubOnlyResolveCount++ + "/main.d", mainFileText);
        PsiReference reference = ReadAction.compute(() -> psiMainFile.findReferenceAt(referencedOffset));
        assertNotNull(reference);
        referencedElement = reference;

        Disposable disposable = Disposable.newDisposable();
        try {
            checkAstNotLoaded(fixture, disposable);
            PsiElement element = ReadAction.compute(reference::resolve);
            if (resolvedFileName != null) {
                assertNotNull(element, "Referenced not resolved");
                assertInstanceOf(DLanguageModule.class, element);
                assertEquals(resolvedFileName,
                    ReadAction.compute(() -> ((DLanguageModule) element).getFile(GlobalSearchScope.EMPTY_SCOPE).getName()));
            }
            else {
                assertNull(element);
            }
        }
        finally {
            Disposer.dispose(disposable);
        }
    }

    private static void checkAstNotLoaded(CodeInsightTestFixture fixture, Disposable disposable) {
        PsiManagerEx.getInstanceEx(fixture.getProject())
            .setAssertOnFileLoadingFilter(file -> !file.getPath().endsWith("main.d"), disposable);
    }

    private static String convertLineSeparators(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }
}

package io.github.intellij.dlanguage.attributes;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.project.Project;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import io.github.intellij.dlanguage.DlangFileType;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by francis on 12/3/2017.
 * <p>
 * Port of upstream {@code DAttributesFinderTestCase.kt}: the files of the test's data directory are created from text
 * in the light project instead of being configured in IntelliJ's fixture.
 */
@ExtendWith(ConsuloProjectLoader.class)
public abstract class DAttributesFinderTestCase {
    private @Nullable PsiElement myElem;

    private Path getTestDataPath(TestInfo testInfo) throws Exception {
        URL resource = Objects.requireNonNull(getClass().getClassLoader().getResource("gold/attributes/" + getTestDirectoryName(testInfo)));
        return Path.of(resource.toURI());
    }

    private List<Path> getTestDataFiles(TestInfo testInfo) throws Exception {
        try (Stream<Path> files = Files.list(getTestDataPath(testInfo))) {
            return files.toList();
        }
    }

    @BeforeEach
    public void setUp(Project project, TestInfo testInfo) throws Exception {
        for (Path file : getTestDataFiles(testInfo)) {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            int offset = text.indexOf("<attrib>");
            if (offset != -1) {
                text = text.replace("<attrib>", "");
                PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(file.getFileName().toString(), DlangFileType.INSTANCE, text);
                myElem = psiFile.findElementAt(offset);
            }
        }
    }

    protected void doTest(DAttributes attribs) {
        doTest(true, attribs);
    }

    protected void doTest(boolean succeed, DAttributes attribs) {
        if (succeed && myElem == null) {
            fail("Could not find reference at caret.");
        }
        assertNotNull(myElem);
        DAttributesFinder finder = new DAttributesFinder(myElem);
        finder.recurseUp();
        if (succeed) {
            assertTrue(attribs.equals(finder.getAttributes()), "expected attribs and actual must be equal");
        }
        else {
            assertFalse(attribs.equals(finder.getAttributes()), "Test should not have suceeded");
        }
    }

    /**
     * IntelliJ's {@code getTestDirectoryName()}: {@code getTestName(true)}, the method name without {@code test} and
     * with its first letter lowercased.
     */
    private static String getTestDirectoryName(TestInfo testInfo) {
        String name = testInfo.getTestMethod().orElseThrow().getName();
        if (name.startsWith("test")) {
            name = name.substring("test".length());
        }
        return name.isEmpty() ? name : Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}

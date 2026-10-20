package io.github.intellij.dlanguage.psi;

import consulo.application.ReadAction;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Samael Bate (singingbush)
 * created on 20/10/18
 * <p>
 * Upstream's {@code LightDlangTestCase.virtualDlangPsiFile} writes the file into the module's source root and wraps it in
 * a {@link DlangPsiFileImpl}; here the file is written into the source root of the headless project, and the
 * assertions run in a read action.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DlangFileTest {

    /**
     * D module names are, by default, the file name with the path and extension stripped off.
     * <a href="https://dlang.org/spec/module.html">module</a>
     */
    @Test
    public void testGetModuleName_shouldBeFilenameWithoutExtension(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "myapp.d", "// no module defined in source");

        assertEquals("myapp.d", ReadAction.compute(dlangFile::getName)); // should be the filename
        assertEquals("myapp", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("myapp", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    /**
     * D module names are, by default, the file name with the path and extension stripped off.
     * <a href="https://dlang.org/spec/module.html">module</a>
     */
    @Test
    public void testGetModuleName_shouldBeFilenameWithoutExtension_IncludingPath(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "awesome/app.d", "// no module defined in source");

        assertEquals("app.d", ReadAction.compute(dlangFile::getName)); // should be the filename
        assertEquals("app", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("app", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    /**
     * D module names are, by default, the file name with the path and extension stripped off.
     * <a href="https://dlang.org/spec/module.html">module</a>
     */
    @Test
    public void testGetModuleName_HonoursTheNameDefinedInSource(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "myapp.d", "module realname;");

        assertEquals("myapp.d", ReadAction.compute(dlangFile::getName)); // should be the filename
        assertEquals("realname", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("realname", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    /**
     * D module names are, by default, the file name with the path and extension stripped off.
     * <a href="https://dlang.org/spec/module.html">module</a>
     */
    @Test
    public void testGetModuleName_HonoursTheNameDefinedInSource_Multipart(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "myapp.d", "module my.app.realname;");

        assertEquals("myapp.d", ReadAction.compute(dlangFile::getName));
        assertEquals("realname", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("my.app.realname", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertEquals("my.app", ReadAction.compute(dlangFile::getPackageName));
    }

    /**
     * <a href="https://dlang.org/spec/module.html">module</a>
     */
    @Test
    public void testGetModuleName_ReturnsProperModuleName_stdio(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture,
            "c/stdio.d",
            "module c.stdio; // module stdio in the c package"
        );

        assertEquals("stdio.d", ReadAction.compute(dlangFile::getName));
        assertEquals("stdio", ReadAction.compute(dlangFile::getModuleName)); // is this right?

        final String fullyQualifiedModuleName = ReadAction.compute(dlangFile::getFullyQualifiedModuleName);
        assertNotNull(fullyQualifiedModuleName);
        assertEquals("c.stdio", fullyQualifiedModuleName);
        assertEquals("c", ReadAction.compute(dlangFile::getPackageName));
    }

    // https://dlang.org/spec/module.html
    @Test
    public void testGetModuleName_ReturnsProperModuleName_foo_bar(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "foo-bar.d", "module foo_bar;");

        assertEquals("foo-bar.d", ReadAction.compute(dlangFile::getName));
        assertEquals("foo_bar", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("foo_bar", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    // https://dlang.org/spec/module.html
    @Test
    public void testGetModuleName_ReturnsProperModuleName_deprecated_foo(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "foo-bar.d", "deprecated module foo;");

        assertEquals("foo-bar.d", ReadAction.compute(dlangFile::getName));
        assertEquals("foo", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("foo", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    // https://dlang.org/spec/module.html
    @Test
    public void testGetModuleName_ReturnsProperModuleName_deprecated_foo_with_message(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "foo-bar.d", "deprecated(\"Please use foo2 instead.\") module foo;");

        assertEquals("foo-bar.d", ReadAction.compute(dlangFile::getName));
        assertEquals("foo", ReadAction.compute(dlangFile::getModuleName));
        assertEquals("foo", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    /**
     * This test is to handle an invalid module name.
     * The file names for packages and modules should be composed only of
     * ASCII lower case letters, digits, and underscores, and should not be a Keyword.
     *
     * <a href="https://dlang.org/spec/module.html">module</a>
     */
    @Test
    public void testGetModuleName_HandlesInvalidModuleName(CodeInsightTestFixture fixture) throws Exception {
        final DlangPsiFileImpl dlangFile = virtualDlangPsiFile(fixture, "foo-bar.d", "");

        assertEquals("foo-bar.d", ReadAction.compute(dlangFile::getName));
        assertNotNull(ReadAction.compute(dlangFile::getModuleName), "Should return file name without extension");
        assertEquals("foo-bar", ReadAction.compute(dlangFile::getModuleName));

        // todo: would probably be worth having a way for DlangFile to indicate if the package/module name is valid or not

        assertEquals("foo-bar", ReadAction.compute(dlangFile::getFullyQualifiedModuleName));
        assertNull(ReadAction.compute(dlangFile::getPackageName));
    }

    private static DlangPsiFileImpl virtualDlangPsiFile(CodeInsightTestFixture fixture, String filename, String content) throws Exception {
        return (DlangPsiFileImpl) fixture.addFileToProject(CodeInsightTestFixture.SOURCE_ROOT + "/" + filename, content);
    }
}

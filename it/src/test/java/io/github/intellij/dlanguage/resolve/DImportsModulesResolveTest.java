package io.github.intellij.dlanguage.resolve;

import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Check that imports modules declaration are correctly resolved
 * This check that no file is loaded for the resolve and only stubs index are used.
 * The checks ensure that the import (at `/*<ref>*&#47;`) points to the file they
 * should (can be null if no resolved expected)
 */
@ExtendWith(HeadlessProjectExtension.class)
public class DImportsModulesResolveTest extends DResolveTestCase {
    private static final String FOO = CodeInsightTestFixture.SOURCE_ROOT + "/foo";

    /**
     * Prepare working directory with by default 1 directory "foo" and 1 file "resolved.d" in it
     */
    private static void setUp(CodeInsightTestFixture fixture) throws Exception {
        fixture.addFileToProject(FOO + "/resolved.d", "");
    }

    @Test
    public void testImportModuleInSubFolderWithoutModuleDeclaration(CodeInsightTestFixture fixture) throws Exception {
        setUp(fixture);
        // Module name without "module ..." declaration is just the file name (without extension) whatever is his location
        doTestStubOnlyResolve(fixture,
            """
            import /*<ref>*/resolved;
            """,
            "resolved.d");
        // Module "foo.resolved" does not exists, only "resolved exists"
        doTestStubOnlyResolve(fixture,
            """
            import foo./*<ref>*/resolved;
            """,
            null);
    }

    @Test
    public void testImportModuleInSubFolderWithModuleDeclaration(CodeInsightTestFixture fixture) throws Exception {
        setUp(fixture);
        fixture.addFileToProject(FOO + "/resolved.d", "module foo.resolved;");
        doTestStubOnlyResolve(fixture,
            """
            import foo./*<ref>*/resolved;
            """,
            "resolved.d");
    }

    @Test
    public void testImportModuleInSubFolderWithCustomModuleDeclaration(CodeInsightTestFixture fixture) throws Exception {
        setUp(fixture);
        // When the module name does not follow the folder architecture, it should still work
        fixture.addFileToProject(FOO + "/resolved.d", "module bar.test;");
        doTestStubOnlyResolve(fixture,
            """
            import bar./*<ref>*/test;
            """,
            "resolved.d");
        // Ensure only 1 way to access to it is possible
        doTestStubOnlyResolve(fixture,
            """
            import foo./*<ref>*/resolved;
            """,
            null);
        doTestStubOnlyResolve(fixture,
            """
            import /*<ref>*/resolved;
            """,
            null);
    }

    @Test
    public void testImportPackageModuleWithoutModuleDeclaration(CodeInsightTestFixture fixture) throws Exception {
        setUp(fixture);
        fixture.addFileToProject(FOO + "/package.d", "");
        doTestStubOnlyResolve(fixture,
            """
            import /*<ref>*/foo;
            """,
            "package.d");
    }

    @Test
    public void testImportPackageModuleWithModuleDeclaration(CodeInsightTestFixture fixture) throws Exception {
        setUp(fixture);
        fixture.addFileToProject(FOO + "/package.d", "module foo;");
        doTestStubOnlyResolve(fixture,
            """
            import /*<ref>*/foo;
            """,
            "package.d");
    }

    @Test
    public void testImportPackageModuleWithCustomModuleDeclaration(CodeInsightTestFixture fixture) throws Exception {
        setUp(fixture);
        // One strange behavior with package.d file is that they by definition are accessible through the package they are in
        // But if you override the module name with whatever else, both names are valid. You can import your file through both names.
        fixture.addFileToProject(FOO + "/package.d", "module bar;");
        doTestStubOnlyResolve(fixture,
            """
            import /*<ref>*/foo;
            """,
            "package.d");
        doTestStubOnlyResolve(fixture,
            """
            import /*<ref>*/bar;
            """,
            "package.d");
    }
}

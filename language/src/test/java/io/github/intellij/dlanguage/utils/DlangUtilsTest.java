package io.github.intellij.dlanguage.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 * created on 14/02/2022
 */
public class DlangUtilsTest {

    @Test
    public void testPackageNameRegex() {
        assertTrue(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("somename").matches());
        assertTrue(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("some_name").matches());
        assertTrue(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("somename2").matches());

        assertFalse(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("some-name").matches());
        assertFalse(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("SomeName").matches());
        assertFalse(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("some.name").matches());
        assertFalse(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("ʊmlaʊt").matches());

        // don't allow keyword
        assertFalse(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("module").matches());
        assertFalse(DlangUtils.VALID_PACKAGENAME_REGEX.matcher("alias").matches());
    }

    @Test
    public void testModuleNameRegex() {
        assertTrue(DlangUtils.VALID_MODULENAME_REGEX.matcher("somename").matches());
        assertTrue(DlangUtils.VALID_MODULENAME_REGEX.matcher("some_name").matches());
        assertTrue(DlangUtils.VALID_MODULENAME_REGEX.matcher("somename2").matches());

        assertFalse(DlangUtils.VALID_MODULENAME_REGEX.matcher("some-name").matches());
        assertFalse(DlangUtils.VALID_MODULENAME_REGEX.matcher("SomeName").matches());
        assertFalse(DlangUtils.VALID_MODULENAME_REGEX.matcher("some.name").matches());
        assertFalse(DlangUtils.VALID_MODULENAME_REGEX.matcher("ʊmlaʊt").matches());

        // don't allow keyword
        assertFalse(DlangUtils.VALID_MODULENAME_REGEX.matcher("module").matches());
        assertFalse(DlangUtils.VALID_MODULENAME_REGEX.matcher("alias").matches());
    }

    @Test
    public void testFileNameRegex() {
        assertTrue(DlangUtils.VALID_FILENAME_REGEX.matcher("somename.d").matches());
        assertTrue(DlangUtils.VALID_FILENAME_REGEX.matcher("some_name.d").matches());
        assertTrue(DlangUtils.VALID_FILENAME_REGEX.matcher("somename2.d").matches());

        assertTrue(DlangUtils.VALID_FILENAME_REGEX.matcher("somename.di").matches());
        assertTrue(DlangUtils.VALID_FILENAME_REGEX.matcher("some_name.di").matches());
        assertTrue(DlangUtils.VALID_FILENAME_REGEX.matcher("somename2.di").matches());

        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("some-name.d").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("SomeName.d").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("some.name.d").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("ʊmlaʊt.d").matches());

        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("some-name.di").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("SomeName.di").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("some.name.di").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("ʊmlaʊt.di").matches());

        // don't allow keyword
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("my.module.d").matches());
        assertFalse(DlangUtils.VALID_FILENAME_REGEX.matcher("module.app.d").matches());
    }

    @Test
    public void testQualifiedNameRegex() {
        assertTrue(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.somename").matches());
        assertTrue(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.some_name").matches());
        assertTrue(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.somename2").matches());

        assertFalse(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.some-name").matches());
        assertFalse(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.SomeName").matches());
        assertFalse(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.some name").matches());
        assertFalse(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.app.ʊmlaʊt").matches());

        // don't allow keyword
        assertFalse(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("my.module").matches());
        assertFalse(DlangUtils.VALID_QUALIFIED_NAME_REGEX.matcher("module.app").matches());
    }
}

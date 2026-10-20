package io.github.intellij.dlanguage.utils;

import org.junit.jupiter.api.Test;

import static io.github.intellij.dlanguage.utils.DUtil.isValidDlangFileName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 * created on 27/10/2019
 */
public class DUtilTest {
    @Test
    public void testIsValidDlangFileName() {
        assertFalse(isValidDlangFileName(""), "should handle empty string");

        assertTrue(isValidDlangFileName("myfile.d"), "should handle valid .d file");
        assertTrue(isValidDlangFileName("myfile.di"), "should handle valid .di file");

        assertTrue(isValidDlangFileName("my_file.d"), "should handle underscore in source file");

        assertFalse(isValidDlangFileName("myfile.da"), "Ignore other file types");
        assertFalse(isValidDlangFileName("myfile.exe"), "Ignore other file types");
        assertFalse(isValidDlangFileName("myfile.c"), "Ignore other file types");

        assertFalse(isValidDlangFileName("my#f'le.d"), "Ignore invalid characters");
    }
}

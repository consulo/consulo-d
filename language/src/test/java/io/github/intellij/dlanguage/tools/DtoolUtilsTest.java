package io.github.intellij.dlanguage.tools;

import consulo.util.lang.SemVer;
import org.junit.jupiter.api.Test;

import static io.github.intellij.dlanguage.tools.DtoolUtils.isSemVer;
import static io.github.intellij.dlanguage.tools.DtoolUtils.toSemVer;
import static io.github.intellij.dlanguage.tools.DtoolUtils.versionPredates;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samael Bate (singingbush)
 * created on 19/08/18
 */
public class DtoolUtilsTest {

    @Test
    public void testVersionPredates() {
        assertFalse(versionPredates(null, null));
        assertFalse(versionPredates(null, ""));
        assertFalse(versionPredates(null, "1.0.0"));

        assertFalse(versionPredates("", null));
        assertFalse(versionPredates("", ""));
        assertFalse(versionPredates("", "1.0.0"));

        assertFalse(versionPredates("2.1", "2.0"));
        assertFalse(versionPredates("2.1.1", "2.0.9"));
        assertFalse(versionPredates("2.1.1", "2.1.1"));

        assertTrue(versionPredates("3.5.4", "4.0"));
        assertTrue(versionPredates("3.5.4", "4.0.1"));
        assertTrue(versionPredates("3.5.4", "3.6.1"));
        assertTrue(versionPredates("3.5.4", "3.5.5"));
    }

    @Test
    public void testToSemVer() {
        assertNull(toSemVer(null));
        assertNull(toSemVer(""));
        assertNull(toSemVer("."));
        assertNull(toSemVer("5135"));

        assertEquals(
            new SemVer("DUB version 1.9.0, built on Jun  7 2018", 1, 9, 0),
            toSemVer("DUB version 1.9.0, built on Jun  7 2018"),
            "Should handle version string returned by DUB"
        );

        //assertEquals(SemVer("v2", 2, 0, 0), "v2".toSemVer())
        assertEquals(new SemVer("1.5", 1, 5, 0), toSemVer("1.5"));
        assertEquals(new SemVer("1.6.", 1, 6, 0), toSemVer("1.6."));
        assertEquals(new SemVer("0.1.0", 0, 1, 0), toSemVer("0.1.0"));
        assertEquals(new SemVer("v0.1.0", 0, 1, 0), toSemVer("v0.1.0"));
        assertEquals(new SemVer("v0.1.1-alpha.0", 0, 1, 1), toSemVer("v0.1.1-alpha.0"));
        assertEquals(new SemVer("v1.1.0-beta", 1, 1, 0), toSemVer("v1.1.0-beta"));
        assertEquals(new SemVer("v0.3.4-rc1", 0, 3, 4), toSemVer("v0.3.4-rc1"));
    }

    @Test
    public void testIsSemVer() {
        assertFalse(isSemVer(null));
        assertFalse(isSemVer(""));
        assertFalse(isSemVer("   "));
        assertFalse(isSemVer("."));
        assertFalse(isSemVer(" . "));
        assertFalse(isSemVer("random.words"));

        assertTrue(isSemVer("0.1.0"));
        assertTrue(isSemVer("v0.1.0"));
        assertTrue(isSemVer("v0.1.1-alpha.0"));
        assertTrue(isSemVer("v1.1.0-beta"));
        assertTrue(isSemVer("v0.3.4-rc1"));
    }
}

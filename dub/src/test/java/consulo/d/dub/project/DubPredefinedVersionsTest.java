package consulo.d.dub.project;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubPredefinedVersionsTest {
    @Test
    public void testLinuxDmd() {
        assertEquals(List.of("linux", "Posix", "X86_64", "DigitalMars"),
            DubPredefinedVersions.of(List.of("linux", "posix"), List.of("x86_64"), "dmd"));
    }

    @Test
    public void testWindowsLdc() {
        assertEquals(List.of("Windows", "X86", "LDC"),
            DubPredefinedVersions.of(List.of("windows"), List.of("x86"), "ldc"));
    }

    @Test
    public void testUnknownNamesAreSkipped() {
        assertEquals(List.of("AArch64", "GNU"),
            DubPredefinedVersions.of(List.of("someos"), List.of("aarch64", "cpu9000"), "gdc"));
    }
}

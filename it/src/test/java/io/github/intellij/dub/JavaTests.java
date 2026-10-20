package io.github.intellij.dub;

import consulo.it.HeadlessApplicationExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Port of upstream {@code JavaTests.java}, which checks that {@link Dub}'s Kotlin companion fields are visible to Java.
 * Upstream's third check, {@code SYSTEM_ID.getReadableName()} being {@code "dub"}, is left out: the readable name is
 * the {@code dub.system.name} localize value ("DUB"), and consulo-it loads no localization.
 */
@ExtendWith(HeadlessApplicationExtension.class)
public class JavaTests {

    @Test
    public void testJavaInterop() {
        assertNotNull(Dub.BALL, "This should compile if annotation correct");
        assertNotNull(Dub.SYSTEM_ID, "This should compile if annotation correct");
    }
}

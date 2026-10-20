package io.github.intellij.dlanguage.attributes;

import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.resolve.processors.parameters.DAttributesFinder;
import org.junit.jupiter.api.Test;

/**
 * @author Samael Bate (singingbush)
 * created on 13/11/17
 */
public class DAttributesFinderTest extends DAttributesFinderTestCase {

    @Test
    public void testPublicImport() {
        doTest(new DAttributes(false, DAttributesFinder.Visibility.PUBLIC, false, false, false, false, false, false, false, false));
    }
}

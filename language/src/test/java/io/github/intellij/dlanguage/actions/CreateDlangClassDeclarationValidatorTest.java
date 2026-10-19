package io.github.intellij.dlanguage.actions;

import consulo.d.localize.DLocalize;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samael Bate (singingbush)
 * created on 13/02/2022
 * <p>
 * Consulo: the messages come from {@code DLocalize}; the light test application loads no LOCALIZE-LIB, so they are
 * compared with what {@code DLocalize} answers rather than with upstream's English text.
 */
public class CreateDlangClassDeclarationValidatorTest {

    private static final String EMPTY_ERROR = DLocalize.actionNewDClassValidatorEmptyName().get();
    private static final String EMPTY_PARTS_ERROR = DLocalize.actionNewDClassValidatorEmptyParts().get();
    private static final String INVALID_NAME_ERROR = DLocalize.actionNewDClassValidatorInvalidName().get();

    @Test
    public void testEmptyName() {
        validateName("", EMPTY_ERROR);
    }

    @Test
    public void testSpaces() {
        validateName("       ", EMPTY_ERROR);
    }

    @Test
    public void testEmptyEnd() {
        validateName("test.", EMPTY_PARTS_ERROR);
    }

    @Test
    public void testEmptyPartInQualified() {
        validateName("a..b", EMPTY_PARTS_ERROR);
    }

    @Test
    public void testModuleWithPackage() {
        validateName("a.b.d", null);
    }

    @Test
    public void testSimpleFile() {
        validateName("foo", null);
    }

    @Test
    public void test() {
        validateName("MyClass", null);
        validateName("ny_class", null);
        validateName("my-class", INVALID_NAME_ERROR);
    }

    @Test
    public void testArroundSpaces() {
        validateName("  space   ", null);
    }

    private void validateName(String name, @Nullable String errorMessage) {
        String actualError = ClassNameValidator.INSTANCE.getErrorText(name);
        assertEquals(errorMessage, actualError, "Invalid error message");
    }
}

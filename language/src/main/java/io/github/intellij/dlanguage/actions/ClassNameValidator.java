package io.github.intellij.dlanguage.actions;

import consulo.d.localize.DLocalize;
import consulo.ui.ex.InputValidatorEx;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Port of the {@code ClassNameValidator} object of upstream {@code CreateDlangClassAction.kt}.
 */
public final class ClassNameValidator implements InputValidatorEx {
    public static final ClassNameValidator INSTANCE = new ClassNameValidator();

    private static final Pattern VALID_MODULE_NAME_REGEX = Pattern.compile("^[A-Za-z_][0-z_.]*$");

    private ClassNameValidator() {
    }

    @Override
    public @Nullable String getErrorText(String inputString) {
        if (inputString.strip().isEmpty()) {
            return DLocalize.actionNewDClassValidatorEmptyName().get();
        }

        String[] parts = inputString.split("\\.", -1);
        for (String part : parts) {
            if (part.strip().isEmpty()) {
                return DLocalize.actionNewDClassValidatorEmptyParts().get();
            }
        }

        if (!VALID_MODULE_NAME_REGEX.matcher(inputString.strip()).matches()) {
            return DLocalize.actionNewDClassValidatorInvalidName().get();
        }

        return null;
    }

    @Override
    public boolean checkInput(String inputString) {
        return true;
    }

    @Override
    public boolean canClose(String inputString) {
        return getErrorText(inputString) == null;
    }
}

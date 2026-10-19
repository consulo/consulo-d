package io.github.intellij.dlanguage.utils;

import java.util.regex.Pattern;

/**
 * Some general utility functions that should be helpful in numerous part of the D Language plugin
 *
 * @author Samael Bate (singingbush)
 * created on 14/02/2022
 */
public final class DlangUtils {
    /**
     * Names for packages, modules, and source files should be composed only of
     * ASCII lower case letters, digits, and underscores, and should not be a Keyword.
     * see: https://dlang.org/spec/module.html
     */
    // todo: inject in the full set of keywords
    public static final Pattern VALID_PACKAGENAME_REGEX = Pattern.compile("^(?!module|alias|string|int)([a-z0-9_]+)$");
    public static final Pattern VALID_MODULENAME_REGEX = VALID_PACKAGENAME_REGEX;
    public static final Pattern VALID_FILENAME_REGEX = Pattern.compile("^(?!module|alias|string|int)([a-z0-9_]+)(?:\\.di|\\.d)?$");
    public static final Pattern VALID_QUALIFIED_NAME_REGEX =
        Pattern.compile("^(?!module|alias|string|int)([a-z0-9_]+)((?:\\.)(?!module|alias|string|int)([a-z0-9_]+))*$");
}

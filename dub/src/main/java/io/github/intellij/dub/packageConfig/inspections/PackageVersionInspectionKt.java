package io.github.intellij.dub.packageConfig.inspections;

import consulo.util.lang.SemVer;
import org.jspecify.annotations.Nullable;

/**
 * Top-level declarations of upstream's {@code PackageVersionInspection.kt}.
 */
public final class PackageVersionInspectionKt {
    private PackageVersionInspectionKt() {
    }

    public static boolean isSemVer(@Nullable String text) {
        if (text == null) {
            return false;
        }
        return toSemVer(text) != null;
    }

    public static @Nullable SemVer toSemVer(String text) {
        int endIndex = text.lastIndexOf("+") > 0 ? text.lastIndexOf("+") : text.length();
        return SemVer.parseFromText(text.substring(0, endIndex));
    }

    /**
     * IntelliJ's {@code StringUtil.isLatinAlphanumeric}, which upstream uses and Consulo's {@code StringUtil} does not
     * have.
     */
    static boolean isLatinAlphanumeric(@Nullable CharSequence str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}

package io.github.intellij.dlanguage.psi.interfaces;

/**
 * Top-level declarations of upstream {@code DVisibility.kt}.
 */
public final class DVisibilityKt {
    private DVisibilityKt() {
    }

    /**
     * Upstream extension property {@code val DVisibility.stubKind}.
     */
    public static DVisibilityStubKind getStubKind(DVisibility visibility) {
        return switch (visibility) {
            case DVisibility.Export export -> DVisibilityStubKind.EXPORT;
            case DVisibility.Public aPublic -> DVisibilityStubKind.PUBLIC;
            case DVisibility.Private aPrivate -> DVisibilityStubKind.PRIVATE;
            case DVisibility.Protected aProtected -> DVisibilityStubKind.PROTECTED;
            case DVisibility.Package aPackage -> {
                // upstream: `if (this.name != null) DVisibilityStubKind.PACKAGE_SPECIFIC` is a statement without effect,
                // so PACKAGE is returned in both cases
                yield DVisibilityStubKind.PACKAGE;
            }
        };
    }
}

package io.github.intellij.dlanguage.psi.interfaces;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represent the visibility of a symbol.
 *
 * @see <a href="https://dlang.org/spec/attribute.html#visibility_attributes">Visibility Attributes</a>
 */
public abstract sealed class DVisibility permits DVisibility.Export, DVisibility.Public, DVisibility.Private,
    DVisibility.Protected, DVisibility.Package {

    private DVisibility() {
    }

    public static final class Export extends DVisibility {
        public static final Export INSTANCE = new Export();

        private Export() {
        }
    }

    public static final class Public extends DVisibility {
        public static final Public INSTANCE = new Public();

        private Public() {
        }
    }

    public static final class Private extends DVisibility {
        public static final Private INSTANCE = new Private();

        private Private() {
        }
    }

    public static final class Protected extends DVisibility {
        public static final Protected INSTANCE = new Protected();

        private Protected() {
        }
    }

    public static final class Package extends DVisibility {
        private final @Nullable String myName;

        public Package(@Nullable String name) {
            myName = name;
        }

        public @Nullable String getName() {
            return myName;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Package that)) {
                return false;
            }
            return Objects.equals(myName, that.myName);
        }

        @Override
        public int hashCode() {
            return myName != null ? myName.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Package(name=" + myName + ")";
        }
    }
}

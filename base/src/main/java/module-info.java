import org.jspecify.annotations.NullMarked;

/**
 * Language-agnostic foundations shared by every other module: icons and localization.
 */
@NullMarked
module consulo.d.base {
    requires transitive consulo.application.api;

    exports consulo.d.icon;
    exports consulo.d.localize;
}

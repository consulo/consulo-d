import org.jspecify.annotations.NullMarked;

/**
 * Remaining IDE support for D; the module that produces the plugin.
 */
@NullMarked
module consulo.d {
    requires consulo.d.base;
    requires consulo.d.language;
    requires consulo.d.sdlang;
    requires consulo.d.dub;
    requires consulo.d.execution;
}

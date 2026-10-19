import org.jspecify.annotations.NullMarked;

/**
 * Running and debugging D programs; debugging comes from the nativeDev plugin.
 */
@NullMarked
module consulo.d.execution {
    requires consulo.d.language;
    requires consulo.nativeDev.debugger.api;
    requires consulo.execution.debug.api;

    exports consulo.d.execution.debugger;
}

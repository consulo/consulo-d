import org.jspecify.annotations.NullMarked;

/**
 * Running, debugging and unit testing D programs; debugging comes from the nativeDev plugin.
 */
@NullMarked
module consulo.d.execution {
    requires consulo.d.language;
    requires consulo.nativeDev.debugger.api;
    requires consulo.execution.debug.api;
    requires consulo.execution.api;
    requires consulo.execution.test.sm.api;
    requires consulo.compiler.api;
    requires consulo.file.chooser.api;
    requires consulo.file.template.api;

    exports consulo.d.execution;
    exports consulo.d.execution.debugger;
    exports consulo.d.execution.fragment;
    exports io.github.intellij.dlanguage.run;
    exports io.github.intellij.dlanguage.run.exception;
    exports io.github.intellij.dlanguage.unittest;

    opens io.github.intellij.dlanguage.run to consulo.util.xml.serializer, consulo.execution.api;
    opens io.github.intellij.dlanguage.unittest to consulo.util.xml.serializer;
}

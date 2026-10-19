import org.jspecify.annotations.NullMarked;

/**
 * DUB support: importing dub.json / dub.sdl projects through the external system.
 */
@NullMarked
module consulo.d.dub {
    requires consulo.d.language;
    requires consulo.d.sdlang;
    requires transitive consulo.external.system.api;
    requires consulo.process.api;
    requires consulo.module.content.api;
    requires consulo.project.ui.api;
    requires consulo.execution.api;
    requires consulo.nativeDev.debugger.api;
    requires com.google.gson;

    exports consulo.d.dub;
    exports consulo.d.dub.importing;
    exports consulo.d.dub.module.extension;
    exports consulo.d.dub.orderEntry;
    exports consulo.d.dub.project;
    exports consulo.d.dub.setting;
    exports consulo.d.dub.task;
    exports io.github.intellij.dub;
    exports io.github.intellij.dub.packageConfig;
    exports io.github.intellij.dub.project;
    exports io.github.intellij.dub.run;
    exports io.github.intellij.dub.service;
    exports io.github.intellij.dub.tools;

    opens consulo.d.dub.setting to consulo.util.xml.serializer;
}

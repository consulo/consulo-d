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
    requires consulo.json.api;
    requires consulo.language.editor.api;
    requires consulo.ide.api;
    requires consulo.compiler.api;
    requires consulo.build.ui.api;

    exports consulo.d.dub;
    exports consulo.d.dub.compiler;
    requires consulo.module.creation.api;

    exports consulo.d.dub.action;
    exports consulo.d.dub.importing;
    exports consulo.d.dub.module.extension;
    exports consulo.d.dub.newProject;
    exports consulo.d.dub.orderEntry;
    exports consulo.d.dub.project;
    exports consulo.d.dub.setting;
    exports consulo.d.dub.task;
    exports io.github.intellij.dlanguage.codeinsight.linemarker;
    exports io.github.intellij.dub;
    exports io.github.intellij.dub.actions;
    exports io.github.intellij.dub.highlighting.annotation;
    exports io.github.intellij.dub.module;
    exports io.github.intellij.dub.packageConfig;
    exports io.github.intellij.dub.packageConfig.inspections;
    exports io.github.intellij.dub.project;
    exports io.github.intellij.dub.project.settings;
    exports io.github.intellij.dub.project.wizard;
    exports io.github.intellij.dub.run;
    exports io.github.intellij.dub.service;
    exports io.github.intellij.dub.toolwindow;
    exports io.github.intellij.dub.tools;

    opens consulo.d.dub.setting to consulo.util.xml.serializer;
}

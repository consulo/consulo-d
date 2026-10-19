import org.jspecify.annotations.NullMarked;

/**
 * The D language: upstream utils, dlang/psi-api, dlang/psi-impl, dlang/idea and the root sources,
 * which share packages and therefore form one module. Upstream packages are kept as is.
 */
@NullMarked
module consulo.d.language {
    requires transitive consulo.d.base;
    requires transitive consulo.language.api;
    requires consulo.language.impl;
    requires consulo.language.editor.api;
    requires consulo.process.api;
    requires consulo.module.content.api;
    requires consulo.module.ui.api;

    exports consulo.d.language;
    exports consulo.d.language.moduleAware;
    exports consulo.d.module.extension;
    exports io.github.intellij.dlanguage;
    exports io.github.intellij.dlanguage.annotator;
    exports io.github.intellij.dlanguage.documentation;
    exports io.github.intellij.dlanguage.documentation.psi;
    exports io.github.intellij.dlanguage.documentation.psi.impl;
    exports io.github.intellij.dlanguage.index;
    exports io.github.intellij.dlanguage.lexer;
    exports io.github.intellij.dlanguage.parser;
    exports io.github.intellij.dlanguage.project;
    exports io.github.intellij.dlanguage.tools;
    exports io.github.intellij.dlanguage.settings;
    exports io.github.intellij.dlanguage.psi;
    exports io.github.intellij.dlanguage.psi.ext;
    exports io.github.intellij.dlanguage.psi.impl;
    exports io.github.intellij.dlanguage.psi.impl.named;
    exports io.github.intellij.dlanguage.psi.interfaces;
    exports io.github.intellij.dlanguage.psi.named;
    exports io.github.intellij.dlanguage.psi.references;
    exports io.github.intellij.dlanguage.psi.resolve.processor;
    exports io.github.intellij.dlanguage.psi.scope;
    exports io.github.intellij.dlanguage.psi.scope.processor;
    exports io.github.intellij.dlanguage.psi.types;
    exports io.github.intellij.dlanguage.resolve;
    exports io.github.intellij.dlanguage.resolve.processors.parameters;
    exports io.github.intellij.dlanguage.stubs;
    exports io.github.intellij.dlanguage.stubs.index;
    exports io.github.intellij.dlanguage.stubs.interfaces;
    exports io.github.intellij.dlanguage.stubs.types;
    exports io.github.intellij.dlanguage.utils;
}

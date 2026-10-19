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
    requires consulo.language.code.style.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.execution.api;
    requires consulo.process.api;
    requires consulo.module.content.api;
    requires consulo.module.ui.api;
    requires consulo.ide.api;
    requires consulo.project.ui.view.api;
    requires consulo.language.editor.refactoring.api;
    requires consulo.file.template.api;
    requires consulo.module.creation.api;
    requires consulo.file.editor.api;

    exports consulo.d.language;
    exports consulo.d.language.completion;
    exports consulo.d.language.messagebus;
    exports consulo.d.language.moduleAware;
    exports consulo.d.module.extension;
    exports consulo.d.newProject;
    exports io.github.intellij.dlanguage;
    exports io.github.intellij.dlanguage.codeinsight;
    exports io.github.intellij.dlanguage.actions;
    exports io.github.intellij.dlanguage.colors;
    exports io.github.intellij.dlanguage.features;
    exports io.github.intellij.dlanguage.features.documentation;
    exports io.github.intellij.dlanguage.features.documentation.psi;
    exports io.github.intellij.dlanguage.features.documentation.psi.impl;
    exports io.github.intellij.dlanguage.features.formatter;
    exports io.github.intellij.dlanguage.features.formatter.impl;
    exports io.github.intellij.dlanguage.features.formatter.settings;
    exports io.github.intellij.dlanguage.folding;
    exports io.github.intellij.dlanguage.highlighting;
    exports io.github.intellij.dlanguage.highlighting.annotation;
    exports io.github.intellij.dlanguage.highlighting.annotation.external;
    exports io.github.intellij.dlanguage.highlighting.exitpoint;
    exports io.github.intellij.dlanguage.annotator;
    exports io.github.intellij.dlanguage.documentation;
    exports io.github.intellij.dlanguage.documentation.psi;
    exports io.github.intellij.dlanguage.documentation.psi.impl;
    exports io.github.intellij.dlanguage.index;
    exports io.github.intellij.dlanguage.inspections;
    exports io.github.intellij.dlanguage.lexer;
    exports io.github.intellij.dlanguage.messagebus;
    exports io.github.intellij.dlanguage.library;
    exports io.github.intellij.dlanguage.module;
    exports io.github.intellij.dlanguage.parser;
    exports io.github.intellij.dlanguage.presentation;
    exports io.github.intellij.dlanguage.processors;
    exports io.github.intellij.dlanguage.project;
    exports io.github.intellij.dlanguage.projectview;
    exports io.github.intellij.dlanguage.project.wizard;
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
    exports io.github.intellij.dlanguage.quickfix;
    exports io.github.intellij.dlanguage.refactoring;
    exports io.github.intellij.dlanguage.resolve;
    exports io.github.intellij.dlanguage.resolve.processors.parameters;
    exports io.github.intellij.dlanguage.structure;
    exports io.github.intellij.dlanguage.stubs;
    exports io.github.intellij.dlanguage.stubs.index;
    exports io.github.intellij.dlanguage.stubs.interfaces;
    exports io.github.intellij.dlanguage.stubs.types;
    exports io.github.intellij.dlanguage.template;
    exports io.github.intellij.dlanguage.utils;

    opens io.github.intellij.dlanguage.folding.impl to consulo.util.xml.serializer;
    opens io.github.intellij.dlanguage.features.formatter to consulo.language.code.style.ui.api, consulo.util.xml.serializer;
}

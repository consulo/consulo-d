package io.github.intellij.dlanguage.sdlang.parser;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import io.github.intellij.dlanguage.sdlang.SimpleDefinitionLanguage;
import io.github.intellij.dlanguage.sdlang.lexer.SDLangLexer;
import io.github.intellij.dlanguage.sdlang.psi.ElementTypesKt;
import io.github.intellij.dlanguage.sdlang.impl.SDLangElementTypesFactory;
import io.github.intellij.dlanguage.sdlang.psi.SDLangFile;

@ExtensionImpl
public class SDLangParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(SimpleDefinitionLanguage.INSTANCE);

    @Override
    public Language getLanguage() {
        return SimpleDefinitionLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new SDLangLexer();
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new SDLangParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return ElementTypesKt.SDLANG_COMMENTS;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return ElementTypesKt.SDLANG_STRINGS;
    }

    @Override
    @RequiredReadAction
    public PsiElement createElement(ASTNode node) {
        return SDLangElementTypesFactory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new SDLangFile(viewProvider);
    }
}

package io.github.intellij.dlanguage;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import io.github.intellij.dlanguage.lexer.DlangLexer;
import io.github.intellij.dlanguage.parser.ParserWrapper;
import io.github.intellij.dlanguage.psi.DTokenSets;
import io.github.intellij.dlanguage.psi.DlangPsiFileImpl;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.stubs.types.DFileStubElementType;

/**
 * Port of upstream {@code DLanguageDefinition.kt}; registered in upstream's {@code plugin.xml} as
 * {@code lang.parserDefinition}.
 */
@ExtensionImpl
public class DLangParserDefinition implements ParserDefinition {
    private final TokenSet myWhiteSpaces = TokenSet.create(TokenType.WHITE_SPACE);
    private final TokenSet myRegularComments =
        TokenSet.create(DlangTypes.LINE_COMMENT, DlangTypes.BLOCK_COMMENT, DlangTypes.NESTING_BLOCK_COMMENT);
    private final TokenSet myDocComments = TokenSet.create(DlangTypes.LINE_DOC, DlangTypes.BLOCK_DOC, DlangTypes.NESTING_BLOCK_DOC);
    private final TokenSet myComments = TokenSet.orSet(myRegularComments, myDocComments);

    public TokenSet getWHITE_SPACES() {
        return myWhiteSpaces;
    }

    public TokenSet getREGULAR_COMMENTS() {
        return myRegularComments;
    }

    public TokenSet getDOC_COMMENTS() {
        return myDocComments;
    }

    public TokenSet getCOMMENTS() {
        return myComments;
    }

    @Override
    public Language getLanguage() {
        return DLanguage.INSTANCE;
    }

    @Override
    public ParserWrapper createParser(LanguageVersion languageVersion) {
        return new ParserWrapper();
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new DlangPsiFileImpl(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return TokenSet.orSet(DTokenSets.STRING_LITERALS, DTokenSets.IES_TOKENS);
    }

    @Override
    public DFileStubElementType getFileNodeType() {
        return DFileStubElementType.INSTANCE;
    }

    @Override
    public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
        return myWhiteSpaces;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new DlangLexer();
    }

    @Override
    @RequiredReadAction
    public PsiElement createElement(ASTNode node) {
        return DlangTypes.Factory.createElement(node);
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return myComments;
    }
}

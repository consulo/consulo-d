package io.github.intellij.dlanguage.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.d.localize.DLocalize;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.ICompositeElementType;
import consulo.language.ast.IErrorCounterReparseableElementType;
import consulo.language.ast.ILightLazyParseableElementType;
import consulo.language.ast.LighterASTNode;
import consulo.language.ast.LighterLazyParseableNode;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.parser.PsiBuilderUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.FlyweightCapableTreeStructure;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionUtil;
import consulo.project.Project;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dlanguage.parser.DLangParser;
import io.github.intellij.dlanguage.psi.impl.DLanguageBlockStatementImpl;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class CodeBlockElementType extends IErrorCounterReparseableElementType
    implements ICompositeElementType, ILightLazyParseableElementType {
    public CodeBlockElementType(String name) {
        super(name, DLanguage.INSTANCE);
    }

    @Override
    public @Nullable ASTNode createNode(@Nullable CharSequence text) {
        return new DLanguageBlockStatementImpl(text);
    }

    @Override
    public ASTNode createCompositeNode() {
        return new DLanguageBlockStatementImpl(null);
    }

    @Override
    @RequiredReadAction
    public @Nullable ASTNode parseContents(ASTNode chameleon) {
        PsiElement psi = chameleon.getRequiredPsi();

        Project project = psi.getProject();
        ParserDefinition parserDefinition = Objects.requireNonNull(ParserDefinition.forLanguage(project.getApplication(), getLanguage()));
        LanguageVersion languageVersion = LanguageVersionUtil.findLanguageVersion(getLanguage(), psi);

        PsiBuilder builder = PsiBuilderFactory.getInstance()
            .createBuilder(
                project,
                chameleon,
                parserDefinition.createLexer(languageVersion),
                getLanguage(),
                languageVersion,
                chameleon.getChars()
            );

        parseStatement(builder);
        return builder.getTreeBuilt().getFirstChildNode();
    }

    @Override
    @RequiredReadAction
    public FlyweightCapableTreeStructure<LighterASTNode> parseContents(LighterLazyParseableNode chameleon) {
        PsiFile psi = Objects.requireNonNull(chameleon.getContainingFile());

        Project project = psi.getProject();
        ParserDefinition parserDefinition = Objects.requireNonNull(ParserDefinition.forLanguage(project.getApplication(), getLanguage()));
        LanguageVersion languageVersion = LanguageVersionUtil.findLanguageVersion(getLanguage(), psi);

        PsiBuilder builder = PsiBuilderFactory.getInstance()
            .createBuilder(
                project,
                chameleon,
                parserDefinition.createLexer(languageVersion),
                getLanguage(),
                languageVersion,
                chameleon.getText()
            );

        parseStatement(builder);
        return builder.getLightTree();
    }

    private void parseStatement(PsiBuilder builder) {
        DLangParser parser = new DLangParser(builder);
        parser.parseBlockStatementDeep();
        // if the pared code is invalid, the parser may not have cover everything
        // ensure to go until the end
        PsiBuilder.Marker error = null;
        if (!builder.eof()) {
            error = builder.mark();
        }
        while (!builder.eof()) {
            builder.advanceLexer();
        }
        if (error != null) {
            error.error(DLocalize.parserUnableToParseStatement());
        }
    }

    @Override
    @RequiredReadAction
    public int getErrorsCount(CharSequence seq, Language fileLanguage, Project project) {
        ParserDefinition parserDefinition = Objects.requireNonNull(ParserDefinition.forLanguage(project.getApplication(), getLanguage()));
        Lexer lexer = parserDefinition.createLexer(LanguageVersionUtil.findDefaultVersion(getLanguage()));
        return PsiBuilderUtil.hasProperBraceBalance(seq, lexer, DlangTypes.OP_BRACES_LEFT, DlangTypes.OP_BRACES_RIGHT)
            ? NO_ERRORS
            : FATAL_ERROR;
    }

    @Override
    public boolean reuseCollapsedTokens() {
        return true;
    }
}

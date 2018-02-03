package consulo.d;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.annotations.RequiredReadAction;
import consulo.lang.LanguageVersion;
import io.github.intellij.dlanguage.dlanguage.DlangLexer;
import io.github.intellij.dlanguage.parser.ParserWrapper;
import io.github.intellij.dlanguage.psi.DlangFile;
import io.github.intellij.dlanguage.psi.DlangTypes;
import io.github.intellij.dlanguage.stubs.types.DFileStubElementType;

/**
 * from kotlin
 */
public class DLangParserDefinition implements ParserDefinition
{
	private static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
	private static final TokenSet COMMENTS = TokenSet.create(DlangTypes.LINE_COMMENT, DlangTypes.BLOCK_COMMENT, DlangTypes.NESTING_BLOCK_COMMENT);

	@NotNull
	@Override
	public Lexer createLexer(@NotNull LanguageVersion languageVersion)
	{
		return new FlexAdapter(new DlangLexer(null));
	}

	@NotNull
	@Override
	public PsiParser createParser(@NotNull LanguageVersion languageVersion)
	{
		return new ParserWrapper();
	}

	@NotNull
	@Override
	public IFileElementType getFileNodeType()
	{
		return DFileStubElementType.INSTANCE;
	}

	@NotNull
	@Override
	public TokenSet getWhitespaceTokens(@NotNull LanguageVersion languageVersion)
	{
		return WHITE_SPACES;
	}

	@NotNull
	@Override
	public TokenSet getCommentTokens(@NotNull LanguageVersion languageVersion)
	{
		return COMMENTS;
	}

	@NotNull
	@Override
	public TokenSet getStringLiteralElements(@NotNull LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Override
	public PsiFile createFile(@NotNull FileViewProvider fileViewProvider)
	{
		return new DlangFile(fileViewProvider);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode node)
	{
		return DlangTypes.Factory.createElement(node);
	}

	@NotNull
	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1)
	{
		return SpaceRequirements.MAY;
	}
}

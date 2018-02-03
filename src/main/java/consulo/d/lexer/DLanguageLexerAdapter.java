package consulo.d.lexer;

import com.intellij.lexer.FlexAdapter;
import io.github.intellij.dlanguage.dlanguage.DlangLexer;

/**
 * from kotlin
 */
public class DLanguageLexerAdapter extends FlexAdapter
{
	public DLanguageLexerAdapter()
	{
		super(new DlangLexer());
	}
}

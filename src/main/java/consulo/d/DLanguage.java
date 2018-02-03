package consulo.d;

import com.intellij.lang.Language;

/**
 * from kotlin
 */
public class DLanguage extends Language
{
	public static final DLanguage INSTANCE = new DLanguage();

	public DLanguage()
	{
		super("D");
	}
}

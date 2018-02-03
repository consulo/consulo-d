package consulo.d;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import com.intellij.AbstractBundle;

/**
 * from kotlin
 */
public class DlangBundle extends AbstractBundle
{
	public static final DlangBundle INSTANCE = new DlangBundle();

	public DlangBundle()
	{
		super("i18n");
	}

	@NotNull
	public String message(@PropertyKey(resourceBundle = "i18n") String key, Object... args)
	{
		return getMessage(key, args);
	}
}

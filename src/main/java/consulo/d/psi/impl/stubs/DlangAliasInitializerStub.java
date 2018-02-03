package consulo.d.psi.impl.stubs;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import consulo.d.resolve.processors.parameters.DAttributes;
import io.github.intellij.dlanguage.psi.named.DlangAliasInitializer;
import io.github.intellij.dlanguage.stubs.DNamedStubBase;

/**
 * from kotlin
 */
public class DlangAliasInitializerStub extends DNamedStubBase<DlangAliasInitializer>
{
	public DlangAliasInitializerStub(StubElement parent, @NotNull IStubElementType elementType, StringRef name, DAttributes attributes)
	{
		super(parent, elementType, name, attributes);
	}

	public DlangAliasInitializerStub(StubElement parent, @NotNull IStubElementType elementType, String name, DAttributes attributes)
	{
		super(parent, elementType, name, attributes);
	}
}

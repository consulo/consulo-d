package consulo.d.module.extension;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.projectRoots.SdkTypeId;
import consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import consulo.roots.ModuleRootLayer;
import io.github.intellij.dlanguage.DlangSdkType;

/**
 * @author VISTALL
 * @since 2018-02-03
 */
public class DModuleExtension extends ModuleExtensionWithSdkImpl<DModuleExtension>
{
	public DModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootLayer)
	{
		super(id, rootLayer);
	}

	@NotNull
	@Override
	public Class<? extends SdkTypeId> getSdkTypeClass()
	{
		return DlangSdkType.class;
	}
}

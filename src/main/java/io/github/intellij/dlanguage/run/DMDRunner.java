package io.github.intellij.dlanguage.run;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.d.module.extension.DModuleExtension;
import io.github.intellij.dlanguage.DlangSdkType;

public class DMDRunner
{
	public boolean isValidModule(@NotNull final Module module)
	{
		final Sdk sdk = ModuleUtilCore.getSdk(module, DModuleExtension.class);
		return sdk != null && (sdk.getSdkType() instanceof DlangSdkType);
	}
}


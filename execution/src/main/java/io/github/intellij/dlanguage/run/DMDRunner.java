package io.github.intellij.dlanguage.run;

import consulo.content.bundle.Sdk;
import consulo.d.module.extension.DModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import io.github.intellij.dlanguage.DlangSdkType;
import org.jspecify.annotations.Nullable;

public class DMDRunner {

    public boolean isValidModule(@Nullable final Module module) {
        if(module == null || module.isDisposed()) {
            return false;
        }
        final @Nullable Sdk sdk =ModuleUtilCore.getSdk(module, DModuleExtension.class);
        return sdk != null && (sdk.getSdkType() instanceof DlangSdkType);
    }

//    public boolean ensureRunnerConfigured(@Nullable final Module module,
//                                          final RunProfile profile,
//                                          final Executor executor, final Project project) throws ExecutionException {
//        return false;  //To change body of implemented methods use File | Settings | File Templates.
//    }
}

package consulo.d.newProject;

import consulo.content.bundle.Sdk;
import consulo.module.creation.NewModuleWizardContextBase;
import org.jspecify.annotations.Nullable;

/**
 * What the D steps of the new project wizard ask: the D compiler (upstream's {@code IntelliJDlangNewProjectWizardData}).
 */
public class DNewModuleContext extends NewModuleWizardContextBase {
    private @Nullable Sdk mySdk;

    public DNewModuleContext(boolean isNewProject) {
        super(isNewProject);
    }

    public @Nullable Sdk getSdk() {
        return mySdk;
    }

    public void setSdk(@Nullable Sdk sdk) {
        mySdk = sdk;
    }
}

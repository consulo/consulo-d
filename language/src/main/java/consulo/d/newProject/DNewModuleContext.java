package consulo.d.newProject;

import consulo.content.bundle.Sdk;
import consulo.module.creation.NewModuleWizardContextBase;
import org.jspecify.annotations.Nullable;

/**
 * What the D steps of the new project wizard ask: the D compiler and whether to add sample code (upstream's
 * {@code IntelliJDlangNewProjectWizardData}).
 */
public class DNewModuleContext extends NewModuleWizardContextBase {
    private @Nullable Sdk mySdk;
    private boolean myAddSampleCode = true;

    public DNewModuleContext(boolean isNewProject) {
        super(isNewProject);
    }

    public @Nullable Sdk getSdk() {
        return mySdk;
    }

    public void setSdk(@Nullable Sdk sdk) {
        mySdk = sdk;
    }

    public boolean isAddSampleCode() {
        return myAddSampleCode;
    }

    public void setAddSampleCode(boolean addSampleCode) {
        myAddSampleCode = addSampleCode;
    }
}

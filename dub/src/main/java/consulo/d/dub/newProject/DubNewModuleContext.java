package consulo.d.dub.newProject;

import consulo.d.newProject.DNewModuleContext;
import io.github.intellij.dub.packageConfig.DubDsl;
import io.github.intellij.dub.project.wizard.DubNewProjectWizardData;
import io.github.intellij.dub.project.wizard.DubNewProjectWizardStep;

/**
 * What the DUB step of the new project wizard asks: the D compiler, the recipe format and the {@code dub init} type.
 */
public class DubNewModuleContext extends DNewModuleContext implements DubNewProjectWizardData {
    private DubDsl myDubDsl = DubDsl.JSON;
    private DubNewProjectWizardStep.DubProjectType myDubProjectType = DubNewProjectWizardStep.DubProjectType.MINIMAL;

    public DubNewModuleContext(boolean isNewProject) {
        super(isNewProject);
    }

    @Override
    public DubDsl getDubDsl() {
        return myDubDsl;
    }

    @Override
    public void setDubDsl(DubDsl dubDsl) {
        myDubDsl = dubDsl;
    }

    @Override
    public DubNewProjectWizardStep.DubProjectType getDubProjectType() {
        return myDubProjectType;
    }

    @Override
    public void setDubProjectType(DubNewProjectWizardStep.DubProjectType dubProjectType) {
        myDubProjectType = dubProjectType;
    }
}

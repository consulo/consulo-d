package consulo.d.dub.importing;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.dub.DubConstants;
import consulo.d.dub.setting.DubProjectSettings;
import consulo.d.dub.setting.DubSettings;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurableFactory;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.project.ProjectManager;
import consulo.ui.annotation.RequiredUIAccess;
import io.github.intellij.dub.project.settings.DubConfigurablePanel;

@ExtensionImpl
public class DubSettingsConfigurableFactory implements ExternalSystemSettingsConfigurableFactory<DubProjectSettings, DubSettings> {
    @Override
    public ProjectSystemId getSystemId() {
        return DubConstants.SYSTEM_ID;
    }

    @Override
    public DubProjectSettings createProjectSettings() {
        return new DubProjectSettings();
    }

    @Override
    public DubSettings createSystemSettings() {
        return new DubSettings(ProjectManager.getInstance().getDefaultProject());
    }

    @Override
    @RequiredUIAccess
    public AbstractExternalProjectSettingsConfigurable<DubProjectSettings> createProjectSettingsConfigurable(
        DubProjectSettings settings,
        ExternalSystemSettingsPlace place
    ) {
        return new DubProjectSettingsConfigurable(settings, place);
    }

    @Override
    @RequiredUIAccess
    public ExternalSystemSettingsConfigurable<DubSettings> createSystemSettingsConfigurable(
        DubSettings settings,
        ExternalSystemSettingsPlace place
    ) {
        return new DubConfigurablePanel(settings);
    }
}

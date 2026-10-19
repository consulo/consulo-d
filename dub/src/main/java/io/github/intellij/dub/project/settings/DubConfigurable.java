package io.github.intellij.dub.project.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.d.dub.DubConstants;
import consulo.d.dub.setting.DubProjectSettings;
import consulo.d.dub.setting.DubSettings;
import consulo.d.dub.setting.DubSettingsListener;
import consulo.externalSystem.service.setting.AbstractExternalSystemConfigurable;
import consulo.project.Project;
import jakarta.inject.Inject;

/**
 * The DUB settings page. Upstream's {@code DubConfigurableProvider} page held only the DUB executable; here the page of
 * the DUB external system lists the linked DUB packages with their own executable
 * ({@code consulo.d.dub.importing.DubProjectSettingsConfigurable}), and upstream's executable field
 * ({@link DubConfigurablePanel}) is its system settings, so there is one DUB page.
 */
@ExtensionImpl
public class DubConfigurable extends AbstractExternalSystemConfigurable<DubProjectSettings, DubSettingsListener, DubSettings>
    implements ProjectConfigurable {

    public static final String ID = "language.d.build.tool.dub";

    @Inject
    public DubConfigurable(Project project) {
        super(project, DubConstants.SYSTEM_ID);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getParentId() {
        return StandardConfigurableIds.EXECUTION_GROUP;
    }
}

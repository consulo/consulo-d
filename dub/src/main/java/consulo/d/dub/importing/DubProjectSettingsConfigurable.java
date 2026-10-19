package consulo.d.dub.importing;

import consulo.d.dub.setting.DubProjectSettings;
import consulo.d.localize.DLocalize;
import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

public class DubProjectSettingsConfigurable extends AbstractExternalProjectSettingsConfigurable<DubProjectSettings> {
    private @Nullable TextBox myExecutableBox;

    public DubProjectSettingsConfigurable(DubProjectSettings settings, ExternalSystemSettingsPlace place) {
        super(settings, place);
    }

    @Override
    @RequiredUIAccess
    protected Component createExtraUIComponent(Disposable uiDisposable) {
        TextBox executableBox = TextBox.create(getSettings().getDubExecutable());
        executableBox.setPlaceholder(DLocalize.dubSettingsExecutablePlaceholder());
        myExecutableBox = executableBox;
        return FormBuilder.create()
            .addLabeled(DLocalize.dubSettingsExecutableLabel(), executableBox)
            .build();
    }

    @Override
    @RequiredUIAccess
    protected boolean isExtraModified() {
        return !getExecutable().equals(getSettings().getDubExecutable());
    }

    @Override
    @RequiredUIAccess
    protected void applyExtra() {
        if (myExecutableBox == null) {
            return;
        }
        getSettings().setDubExecutable(getExecutable());
    }

    @RequiredUIAccess
    private String getExecutable() {
        String value = myExecutableBox == null ? null : myExecutableBox.getValue();
        return value == null ? "" : value.trim();
    }
}

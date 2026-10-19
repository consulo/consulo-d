package consulo.d.dub.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.d.dub.DubConstants;
import consulo.externalSystem.setting.AbstractExternalSystemLocalSettings;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "DubLocalSettings", storages = @Storage(file = StoragePathMacros.WORKSPACE_FILE))
public class DubLocalSettings extends AbstractExternalSystemLocalSettings
    implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {

    @Inject
    public DubLocalSettings(Project project) {
        super(DubConstants.SYSTEM_ID, project);
    }

    public static DubLocalSettings getInstance(Project project) {
        return project.getInstance(DubLocalSettings.class);
    }

    @Override
    public State getState() {
        State state = new State();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(State state) {
        super.loadState(state);
    }
}

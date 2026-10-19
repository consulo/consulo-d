package consulo.d.dub.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.DelegatingExternalSystemSettingsListener;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.project.Project;
import consulo.util.xml.serializer.annotation.AbstractCollection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.TreeSet;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "DubSettings", storages = @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dub.xml"))
public class DubSettings extends AbstractExternalSystemSettings<DubSettings, DubProjectSettings, DubSettingsListener>
    implements PersistentStateComponent<DubSettings.MyState> {

    @Inject
    public DubSettings(Project project) {
        super(DubSettingsListener.TOPIC, project);
    }

    public static DubSettings getInstance(Project project) {
        return project.getInstance(DubSettings.class);
    }

    @Override
    public void subscribe(ExternalSystemSettingsListener<DubProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject()).subscribe(DubSettingsListener.TOPIC, new ListenerAdapter(listener));
    }

    @Override
    protected void copyExtraSettingsFrom(DubSettings settings) {
    }

    @Override
    protected void checkSettings(DubProjectSettings old, DubProjectSettings current) {
    }

    @Override
    public MyState getState() {
        MyState state = new MyState();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(MyState state) {
        super.loadState(state);
    }

    private static class ListenerAdapter extends DelegatingExternalSystemSettingsListener<DubProjectSettings>
        implements DubSettingsListener {
        ListenerAdapter(ExternalSystemSettingsListener<DubProjectSettings> delegate) {
            super(delegate);
        }
    }

    public static class MyState implements State<DubProjectSettings> {
        private final Set<DubProjectSettings> myProjectSettings = new TreeSet<>();

        @Override
        @AbstractCollection(surroundWithTag = false, elementTypes = {DubProjectSettings.class})
        public Set<DubProjectSettings> getLinkedExternalProjectsSettings() {
            return myProjectSettings;
        }

        @Override
        public void setLinkedExternalProjectsSettings(@Nullable Set<DubProjectSettings> settings) {
            if (settings != null) {
                myProjectSettings.addAll(settings);
            }
        }
    }
}

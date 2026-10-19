package consulo.d.dub.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

import java.util.Collection;
import java.util.Set;

@TopicAPI(ComponentScope.PROJECT)
public interface DubSettingsListener extends ExternalSystemSettingsListener<DubProjectSettings> {
    Class<DubSettingsListener> TOPIC = DubSettingsListener.class;

    @Override
    default void onProjectRenamed(String oldName, String newName) {
    }

    @Override
    default void onProjectsLinked(Collection<DubProjectSettings> settings) {
    }

    @Override
    default void onProjectsUnlinked(Set<String> linkedProjectPaths) {
    }

    @Override
    default void onBulkChangeStart() {
    }

    @Override
    default void onBulkChangeEnd() {
    }
}

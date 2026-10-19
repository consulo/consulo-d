package consulo.d.dub;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;
import io.github.intellij.dub.Dub;

import java.util.function.Consumer;

@ExtensionImpl
public class DubNotificationGroupContributor implements NotificationGroupContributor {
    public static final NotificationGroup GROUP = NotificationGroup.balloonGroup(Dub.NOTIFICATION_GROUP_ID, DLocalize.dubSystemName());

    @Override
    public void contribute(Consumer<NotificationGroup> registrator) {
        registrator.accept(GROUP);
    }
}

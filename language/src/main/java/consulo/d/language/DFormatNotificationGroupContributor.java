package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;

import java.util.function.Consumer;

/**
 * The {@code Dfmt Action} notification group, which upstream declares in its {@code plugin.xml}.
 */
@ExtensionImpl
public class DFormatNotificationGroupContributor implements NotificationGroupContributor {
    public static final NotificationGroup GROUP = NotificationGroup.balloonGroup("Dfmt Action", DLocalize.dfmtNotificationGroup());

    @Override
    public void contribute(Consumer<NotificationGroup> registrator) {
        registrator.accept(GROUP);
    }
}

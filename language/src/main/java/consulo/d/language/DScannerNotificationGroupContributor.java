package consulo.d.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;

import java.util.function.Consumer;

/**
 * The {@code DScanner Error} notification group upstream declares in {@code plugin.xml}.
 */
@ExtensionImpl
public class DScannerNotificationGroupContributor implements NotificationGroupContributor {
    public static final NotificationGroup GROUP = NotificationGroup.balloonGroup("DScanner Error", DLocalize.dscannerError());

    @Override
    public void contribute(Consumer<NotificationGroup> registrator) {
        registrator.accept(GROUP);
    }
}

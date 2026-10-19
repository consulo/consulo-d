package consulo.d.execution;

import consulo.annotation.component.ExtensionImpl;
import consulo.d.localize.DLocalize;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;

import java.util.function.Consumer;

/**
 * The notification groups of the D run configurations and unit tests, which upstream declared in {@code plugin.xml}.
 */
@ExtensionImpl
public class DExecutionNotificationGroupContributor implements NotificationGroupContributor {
    public static final NotificationGroup EXECUTE_TESTS =
        NotificationGroup.balloonGroup("Execute Tests", DLocalize.notificationGroupExecuteTests());
    public static final NotificationGroup DUNIT_TEST_RUNNER =
        NotificationGroup.balloonGroup("Dunit Test Runner", DLocalize.notificationGroupDunitTestRunner());
    public static final NotificationGroup DMD_RUN_CONFIGURATION =
        NotificationGroup.balloonGroup("DMD run configuration", DLocalize.notificationGroupDmdRunConfiguration());
    public static final NotificationGroup D_APP_RUN_CONFIGURATION =
        NotificationGroup.balloonGroup("D App run configuration", DLocalize.notificationGroupDAppRunConfiguration());

    @Override
    public void contribute(Consumer<NotificationGroup> registrator) {
        registrator.accept(EXECUTE_TESTS);
        registrator.accept(DUNIT_TEST_RUNNER);
        registrator.accept(DMD_RUN_CONFIGURATION);
        registrator.accept(D_APP_RUN_CONFIGURATION);
    }
}

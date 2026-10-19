package io.github.intellij.dub;

import consulo.d.dub.DubConstants;
import consulo.d.icon.DIconGroup;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.ui.image.Image;

public final class Dub {
    public static final ProjectSystemId SYSTEM_ID = DubConstants.SYSTEM_ID;
    public static final Image BALL = DIconGroup.dubBall();
    public static final String NOTIFICATION_GROUP_ID = "dub-notifications";

    private Dub() {
    }
}

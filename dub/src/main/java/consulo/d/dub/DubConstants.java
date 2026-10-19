package consulo.d.dub;

import consulo.d.localize.DLocalize;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.d.icon.DIconGroup;

import java.util.List;

public interface DubConstants {
    ProjectSystemId SYSTEM_ID = new ProjectSystemId("DUB", DLocalize.dubSystemName(), DIconGroup.dubBall());

    String DUB_JSON = "dub.json";
    String DUB_SDL = "dub.sdl";
    List<String> MANIFESTS = List.of(DUB_JSON, DUB_SDL);
    String SELECTIONS = "dub.selections.json";
    String DUB_DIRECTORY = ".dub";

    String DEFAULT_EXECUTABLE = "dub";

    /**
     * Option of the DUB module extension holding the executable the root package builds to.
     */
    String TARGET_EXECUTABLE_OPTION = "dub.target.executable";

    String TASK_BUILD = "build";
    String TASK_RUN = "run";
    String TASK_TEST = "test";
    String TASK_CLEAN = "clean";
    String TASK_UPGRADE = "upgrade";
    String TASK_FETCH = "fetch";
}

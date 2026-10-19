package consulo.d.dub.task;

import consulo.d.dub.DubConstants;
import consulo.d.dub.DubUtil;
import consulo.d.dub.setting.DubExecutionSettings;
import consulo.d.localize.DLocalize;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DubTaskManager implements ExternalSystemTaskManager<DubExecutionSettings> {
    private static final Set<String> TASKS = Set.of(
        DubConstants.TASK_BUILD,
        DubConstants.TASK_RUN,
        DubConstants.TASK_TEST,
        DubConstants.TASK_CLEAN,
        DubConstants.TASK_UPGRADE,
        DubConstants.TASK_FETCH
    );

    private final DubCommand myCommand = new DubCommand();

    @Override
    public void executeTasks(ExternalSystemTaskId id,
                             List<String> taskNames,
                             String projectPath,
                             @Nullable DubExecutionSettings settings,
                             List<String> vmOptions,
                             List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        String executable = settings == null ? DubConstants.DEFAULT_EXECUTABLE : settings.getDubExecutable();
        Path packageDirectory = DubUtil.toPackageDirectory(Path.of(projectPath));
        for (String taskName : taskNames) {
            if (!TASKS.contains(taskName)) {
                throw new ExternalSystemException(DLocalize.dubErrorUnknownTask(taskName).get());
            }
            List<String> arguments = new ArrayList<>();
            arguments.add(taskName);
            arguments.addAll(scriptParameters);
            myCommand.run(id, executable, packageDirectory, arguments, true, listener);
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) {
        return myCommand.cancel();
    }
}

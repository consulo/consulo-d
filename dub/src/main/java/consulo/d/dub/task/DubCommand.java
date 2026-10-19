package consulo.d.dub.task;

import consulo.d.localize.DLocalize;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs {@code dub} in a package directory, streaming its output to the external system console.
 */
public class DubCommand {
    private volatile @Nullable ProcessHandler myProcessHandler;
    private volatile boolean myCancelled;

    /**
     * @return what the command printed to stdout
     */
    public String run(ExternalSystemTaskId id,
                      String dubExecutable,
                      Path packageDirectory,
                      List<String> arguments,
                      boolean echoStdout,
                      ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(dubExecutable);
        commandLine.addParameters(arguments);
        commandLine.withWorkingDirectory(packageDirectory);

        String presentation = "dub " + String.join(" ", arguments);
        listener.onTaskOutput(id, "> " + presentation + "\n", true);

        ProcessHandler processHandler;
        try {
            processHandler = ProcessHandlerBuilder.create(commandLine).killable().build();
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException(DLocalize.dubErrorCannotRun(presentation, String.valueOf(e.getMessage())).get());
        }

        StringBuffer stdout = new StringBuffer();
        AtomicInteger exitCode = new AtomicInteger(-1);
        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType == ProcessOutputTypes.STDOUT) {
                    stdout.append(event.getText());
                    if (echoStdout) {
                        listener.onTaskOutput(id, event.getText(), true);
                    }
                }
                else if (outputType == ProcessOutputTypes.STDERR) {
                    listener.onTaskOutput(id, event.getText(), false);
                }
            }

            @Override
            public void processTerminated(ProcessEvent event) {
                exitCode.set(event.getExitCode());
            }
        });

        myProcessHandler = processHandler;
        try {
            processHandler.startNotify();
            processHandler.waitFor();
        }
        finally {
            myProcessHandler = null;
        }

        if (myCancelled) {
            throw new ExternalSystemException(DLocalize.dubErrorCancelled(presentation).get());
        }
        int code = exitCode.get();
        if (code != 0) {
            throw new ExternalSystemException(DLocalize.dubErrorCommandFailed(presentation, code).get());
        }
        return stdout.toString();
    }

    public boolean cancel() {
        myCancelled = true;
        ProcessHandler processHandler = myProcessHandler;
        if (processHandler == null) {
            return false;
        }
        processHandler.destroyProcess();
        return true;
    }
}

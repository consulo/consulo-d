package io.github.intellij.dub.run;

import consulo.d.dub.DubConstants;
import consulo.d.dub.DubNotificationGroupContributor;
import consulo.d.dub.DubUtil;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationService;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Port of upstream {@code DlangRunDubState.kt}.
 */
public class DlangRunDubState extends CommandLineState {
    private final DlangRunDubConfiguration myConfig;

    DlangRunDubState(ExecutionEnvironment environment, DlangRunDubConfiguration config) {
        super(environment);
        myConfig = config;
    }

    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        try {
            GeneralCommandLine dubCommandLine = getExecutableCommandLine(myConfig);
            ProcessHandler handler = getEnvironment().getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
                .newBuilder(dubCommandLine)
                .killable()
                .colored()
                .build();
            ProcessTerminatedListener.attach(handler, myConfig.getProject());
            return handler;
        }
        catch (ExecutionException e) {
            String message = String.valueOf(e.getMessage());
            Project project = myConfig.getProject();
            boolean isEmpty = message.equals(DLocalize.dubRunExecutableNotSpecified().get());
            boolean notCorrect = message.startsWith("Cannot run program");
            if (isEmpty || notCorrect) {
                NotificationService.getInstance().newError(DubNotificationGroupContributor.GROUP)
                    .title(DLocalize.dubRunNotificationTitle())
                    .content(isEmpty ? DLocalize.dubRunExecutableNotSpecified() : DLocalize.dubRunExecutableNotConfigured())
                    .notify(project);
            }
            throw e;
        }
    }

    /* Build command line to start DUB executable
     */
    private GeneralCommandLine getExecutableCommandLine(DlangRunDubConfiguration config) throws ExecutionException {
        Module module = config.getConfigurationModule().getModule();
        if (module == null) {
            throw new ExecutionException(DLocalize.dubRunNoModule().get());
        }
        Path packageDirectory = DubUtil.findPackageDirectory(module);
        String dubPath = packageDirectory != null
            ? DubUtil.createExecutionSettings(config.getProject(), packageDirectory.toString()).getDubExecutable()
            : DubConstants.DEFAULT_EXECUTABLE;
        if (StringUtil.isEmptyOrSpaces(dubPath)) {
            throw new ExecutionException(DLocalize.dubRunExecutableNotSpecified().get());
        }
        if (Paths.get(dubPath).isAbsolute() && !Paths.get(dubPath).toFile().canExecute()) {
            throw new ExecutionException(DLocalize.dubRunExecutableNotConfigured().get());
        }
        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(dubPath);
        String workingDirectory = config.getProgramSettings().getWorkingDirectory();
        if (StringUtil.isNotEmpty(workingDirectory)) {
            cmd.withWorkDirectory(workingDirectory);
        }
        else if (packageDirectory != null) {
            cmd.withWorkingDirectory(packageDirectory);
        }
        else {
            cmd.withWorkDirectory(config.getProject().getBasePath());
        }

        // Add command line parameters
        boolean toBuild = config.getGeneralDubOptions() == 0;
        boolean toRun = config.getGeneralDubOptions() == 1;
        boolean toTest = config.getGeneralDubOptions() == 2;
        if (toBuild) {
            cmd.addParameter("build");
        }
        else if (toTest) {
            cmd.addParameter("test");
        }
        else if (toRun) {
            cmd.addParameter("run");
        }
        if (toRun) {
            if (config.isCbTempBuild()) {
                cmd.addParameter("--temp-build");
            }
        }
        if (toTest) {
            if (config.isCbCoverage()) {
                cmd.addParameter("--coverage");
            }
            String mainFile = config.getTfMainFile();
            if (StringUtil.isNotEmpty(mainFile)) {
                cmd.addParameter("--main-file");
                cmd.addParameter(mainFile);
            }
        }
        if (config.isCbRdmd()) {
            cmd.addParameter("--rdmd");
        }
        if (config.isCbForce()) {
            cmd.addParameter("--force");
        }
        if (config.isCbNoDeps()) {
            cmd.addParameter("--nodeps");
        }
        if (config.isCbForceRemove()) {
            cmd.addParameter("--force-remove");
        }
        if (config.isCbCombined()) {
            cmd.addParameter("--combined");
        }
        if (config.isCbParallel()) {
            cmd.addParameter("--parallel");
        }
        if (config.isQuiet()) {
            cmd.addParameter("-q");
        }
        if (config.isVerbose()) {
            cmd.addParameter("-v");
        }
        addOption(cmd, "--arch", config.getTfArch());
        addOption(cmd, "--build", config.getTfBuild());
        addOption(cmd, "--config", config.getTfConfig());
        addOption(cmd, "--debug", config.getTfDebug());
        addOption(cmd, "--compiler", config.getTfCompiler());
        boolean bmSeparate = config.getBuildMode() == 0;
        boolean bmAll = config.getBuildMode() == 1;
        boolean bmSingle = config.getBuildMode() == 2;
        if (bmSeparate) {
            cmd.addParameter("--build-mode");
            cmd.addParameter("separate");
        }
        if (bmAll) {
            cmd.addParameter("--build-mode");
            cmd.addParameter("allAtOnce");
        }
        if (bmSingle) {
            cmd.addParameter("--build-mode");
            cmd.addParameter("singleFile");
        }
        String additionalParams = config.getAdditionalParams();
        if (additionalParams != null && DUtil.isNotNullOrEmpty(additionalParams)) {
            cmd.addParameters(Arrays.stream(additionalParams.split("\\s")).filter(s -> !s.isEmpty()).toList());
        }
        List<String> programArguments = config.getProgramSettings().getProgramArguments();
        if (toRun && !programArguments.isEmpty()) {
            cmd.addParameter("--");
            cmd.addParameters(programArguments);
        }
        cmd.withEnvironment(config.getProgramSettings().getEnvs());
        cmd.withParentEnvironmentType(config.getProgramSettings().isPassParentEnvs()
            ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
            : GeneralCommandLine.ParentEnvironmentType.NONE);
        return cmd;
    }

    private static void addOption(GeneralCommandLine cmd, String option, @Nullable String value) {
        if (value != null && DUtil.isNotNullOrEmpty(value)) {
            cmd.addParameter(option);
            cmd.addParameter(value);
        }
    }
}

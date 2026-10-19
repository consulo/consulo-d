package consulo.d.dub.compiler;

import consulo.annotation.component.ExtensionImpl;
import consulo.build.ui.FilePosition;
import consulo.build.ui.event.MessageEvent;
import consulo.build.ui.progress.BuildProgress;
import consulo.build.ui.progress.BuildProgressDescriptor;
import consulo.compiler.CompileContextEx;
import consulo.compiler.CompileDriver;
import consulo.compiler.CompilerRunner;
import consulo.d.dub.DubUtil;
import consulo.d.localize.DLocalize;
import consulo.dataContext.DataContext;
import consulo.execution.configuration.RunConfiguration;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;
import io.github.intellij.dub.run.DlangRunDubConfiguration;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the DUB packages of the project with {@code dub build}: Build > Make / Rebuild, and the "Build" step before a
 * DUB run configuration is launched (so Debug, which starts the built executable, never runs a stale one). For a DUB run
 * configuration its {@code --config}, {@code --build}, {@code --compiler}, {@code --arch}, {@code --debug} and build mode
 * are used; a rebuild adds {@code --force}.
 */
@ExtensionImpl
public class DubCompilerRunner implements CompilerRunner {
    // dmd, ldc2: source/app.d(19,5): Error: undefined identifier `foo`
    private static final Pattern DMD_MESSAGE = Pattern.compile("^(.+?\\.di?)\\((\\d+)(?:,(\\d+))?\\): (Error|Warning|Deprecation): (.*)$");
    // gdc: source/app.d:19:5: error: undefined identifier 'foo'
    private static final Pattern GDC_MESSAGE = Pattern.compile("^(.+?\\.di?):(\\d+):(\\d+): (error|warning|note): (.*)$");

    private final Project myProject;

    @Inject
    public DubCompilerRunner(Project project) {
        myProject = project;
    }

    @Override
    public LocalizeValue getName() {
        return DLocalize.dubSystemName();
    }

    @Override
    public Result checkAvailable(DataContext dataContext) {
        // the data context is a view over the compile scope: a run configuration is present when the build was started to
        // launch something, and a configuration that is not DUB's is left to the runner that owns it
        RunConfiguration runConfiguration = dataContext.getData(RunConfiguration.KEY);
        if (runConfiguration != null && !(runConfiguration instanceof DlangRunDubConfiguration)) {
            return NO;
        }
        if (dubModules().isEmpty()) {
            return NO;
        }
        return new YesResult(PlatformIconGroup.actionsCompile());
    }

    @Override
    public boolean build(
        CompileDriver compileDriver,
        CompileContextEx context,
        BuildProgress<BuildProgressDescriptor> buildProgress,
        boolean isRebuild,
        boolean forceCompile,
        boolean onlyCheckStatus
    ) {
        if (onlyCheckStatus) {
            // DUB decides for itself what is out of date
            return false;
        }

        RunConfiguration runConfiguration = context.getCompileScope().getUserData(RunConfiguration.KEY);
        List<Module> modules;
        DlangRunDubConfiguration dubConfiguration = null;
        if (runConfiguration instanceof DlangRunDubConfiguration configuration) {
            dubConfiguration = configuration;
            Module module = configuration.getConfigurationModule().getModule();
            if (module == null) {
                context.newError(DLocalize.dubRunNoModule()).add();
                return false;
            }
            modules = List.of(module);
        }
        else {
            modules = dubModules();
        }

        for (Module module : modules) {
            Path packageDirectory = DubUtil.findPackageDirectory(module);
            if (packageDirectory == null) {
                context.newError(DLocalize.dubCompilerNoPackage(module.getName())).add();
                return false;
            }
            GeneralCommandLine commandLine = commandLine(packageDirectory, dubConfiguration, isRebuild);
            context.getProgressIndicator().setText(LocalizeValue.of(commandLine.getCommandLineString()));
            if (run(commandLine, packageDirectory, context, buildProgress) != 0) {
                return false;
            }
        }
        return true;
    }

    private List<Module> dubModules() {
        List<Module> modules = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(myProject).getModules()) {
            if (DubUtil.findPackageDirectory(module) != null) {
                modules.add(module);
            }
        }
        return modules;
    }

    private GeneralCommandLine commandLine(Path packageDirectory, @Nullable DlangRunDubConfiguration configuration, boolean isRebuild) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(DubUtil.createExecutionSettings(myProject, packageDirectory.toString()).getDubExecutable());
        commandLine.withWorkingDirectory(packageDirectory);
        commandLine.addParameter("build");
        if (isRebuild) {
            commandLine.addParameter("--force");
        }
        if (configuration != null) {
            addOption(commandLine, "--arch", configuration.getTfArch());
            addOption(commandLine, "--build", configuration.getTfBuild());
            addOption(commandLine, "--config", configuration.getTfConfig());
            addOption(commandLine, "--debug", configuration.getTfDebug());
            addOption(commandLine, "--compiler", configuration.getTfCompiler());
            String buildMode = switch (configuration.getBuildMode()) {
                case 1 -> "allAtOnce";
                case 2 -> "singleFile";
                default -> "separate";
            };
            commandLine.addParameters("--build-mode", buildMode);
        }
        return commandLine;
    }

    private static void addOption(GeneralCommandLine commandLine, String option, @Nullable String value) {
        if (!StringUtil.isEmptyOrSpaces(value)) {
            commandLine.addParameters(option, value);
        }
    }

    private static int run(
        GeneralCommandLine commandLine,
        Path packageDirectory,
        CompileContextEx context,
        BuildProgress<BuildProgressDescriptor> buildProgress
    ) {
        ProcessHandler processHandler;
        try {
            processHandler = ProcessHandlerBuilder.create(commandLine).killable().build();
        }
        catch (ExecutionException e) {
            context.newError(LocalizeValue.ofNullable(e.getMessage())).add();
            return -1;
        }
        int[] exitCode = {-1};
        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType == ProcessOutputTypes.SYSTEM) {
                    // the process handler's own command line echo, printed once below
                    return;
                }
                String text = event.getText();
                buildProgress.output(text, outputType != ProcessOutputTypes.STDERR);
                for (String line : StringUtil.splitByLines(text)) {
                    report(line, packageDirectory, buildProgress);
                }
            }

            @Override
            public void processTerminated(ProcessEvent event) {
                exitCode[0] = event.getExitCode();
            }
        });
        buildProgress.output(commandLine.getCommandLineString() + "\n", true);
        processHandler.startNotify();
        while (!processHandler.waitFor(100)) {
            if (context.getProgressIndicator().isCanceled()) {
                processHandler.destroyProcess();
                return -1;
            }
        }
        return exitCode[0];
    }

    private static void report(String line, Path packageDirectory, BuildProgress<BuildProgressDescriptor> buildProgress) {
        Matcher matcher = DMD_MESSAGE.matcher(line);
        if (!matcher.matches()) {
            matcher = GDC_MESSAGE.matcher(line);
            if (!matcher.matches()) {
                return;
            }
        }
        MessageEvent.Kind kind = switch (matcher.group(4)) {
            case "Error", "error" -> MessageEvent.Kind.ERROR;
            case "Warning", "warning", "Deprecation" -> MessageEvent.Kind.WARNING;
            default -> MessageEvent.Kind.INFO;
        };
        int lineNumber = Integer.parseInt(matcher.group(2)) - 1;
        String column = matcher.group(3);
        int columnNumber = column == null ? 0 : Integer.parseInt(column) - 1;
        Path file = packageDirectory.resolve(matcher.group(1)).normalize();
        LocalizeValue message = LocalizeValue.of(matcher.group(5));
        buildProgress.fileMessage(message, message, kind, new FilePosition(file.toFile(), lineNumber, columnNumber));
    }
}

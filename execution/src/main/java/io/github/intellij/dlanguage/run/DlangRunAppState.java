package io.github.intellij.dlanguage.run;

import consulo.compiler.ModuleCompilerPathsManager;
import consulo.content.bundle.Sdk;
import consulo.d.execution.DExecutionNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.util.CommandLineTokenizer;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.localize.ProcessLocalize;
import consulo.project.ui.notification.NotificationService;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import io.github.intellij.dlanguage.DlangSdkType;
import io.github.intellij.dlanguage.run.exception.ModuleNotFoundException;
import io.github.intellij.dlanguage.run.exception.NoValidDlangSdkFound;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * Consulo: the environment of the configuration (nativeDev's program settings, which the debugger launches with too)
 * is passed to the program; upstream never applied its {@code envVars}.
 */
public class DlangRunAppState extends CommandLineState {
    private final DlangRunAppConfiguration config;

    protected DlangRunAppState(final ExecutionEnvironment environment, final DlangRunAppConfiguration config) {
        super(environment);
        this.config = config;
    }

    @Override
    public ProcessHandler startProcess() throws ExecutionException {
        try {
            final GeneralCommandLine appCommandLine = getExecutableCommandLine(config);
            final ProcessHandler handler = config.getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
                .newBuilder(appCommandLine)
                .colored()
                .build();
            ProcessTerminatedListener.attach(handler, config.getProject());
            return handler;
        } catch (final NoValidDlangSdkFound e) {
            throw new ExecutionException(DLocalize.runAppNoValidSdk().get());
        } catch (final ModuleNotFoundException e) {
            throw new ExecutionException(DLocalize.runNoModuleSelected().get());
        } catch (final ExecutionException e) {
            final String message = String.valueOf(e.getMessage());
            final boolean isEmpty = message.equals(ProcessLocalize.runConfigurationErrorExecutableNotSpecified().get());
            final boolean notCorrect = message.startsWith("Cannot run program");

            if (isEmpty || notCorrect) {
                NotificationService.getInstance()
                    .newError(DExecutionNotificationGroupContributor.D_APP_RUN_CONFIGURATION)
                    .title(DLocalize.runAppNotificationTitle())
                    .content(isEmpty ? DLocalize.runAppNotificationExecutableEmpty() : DLocalize.runAppNotificationExecutableNotCorrect())
                    .notify(config.getProject());
            }
            throw e;
        }
    }

    /* Build command line to start compiled executable
     **/
    public GeneralCommandLine getExecutableCommandLine(final DlangRunAppConfiguration config)
        throws ModuleNotFoundException, NoValidDlangSdkFound {
        final Module module = config.getConfigurationModule().getModule();
        if (module == null) {
            throw new ModuleNotFoundException();
        }

        final Sdk sdk = ModuleUtilCore.getSdk(module, DModuleExtension.class);
        if (sdk == null || !(sdk.getSdkType() instanceof DlangSdkType)) {
            throw new NoValidDlangSdkFound();
        }

        final VirtualFile sourcesRoot = getSourceRoot(module);
        final GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(getOutputFilePath(module));

        final @Nullable String workDir = config.getWorkingDirectory();
        if (StringUtil.isNotEmpty(workDir)) {
            cmd.withWorkDirectory(workDir);
        } else {
            cmd.withWorkDirectory(Objects.requireNonNull(sourcesRoot).getPath());
        }

        if (StringUtil.isNotEmpty(config.getProgramParameters())) {
            cmd.addParameters(splitArguments(config.getProgramParameters()));
        }

        cmd.withEnvironment(config.getEnvs());
        cmd.withParentEnvironmentType(config.isPassParentEnvs()
            ? GeneralCommandLine.ParentEnvironmentType.CONSOLE
            : GeneralCommandLine.ParentEnvironmentType.NONE);

        return cmd;
    }

    private String[] splitArguments(@Nullable final String arguments) {
        if(arguments != null) {
            final List<String> argsLst = new LinkedList<>();
            final CommandLineTokenizer tokenizer = new CommandLineTokenizer(arguments);
            while (tokenizer.hasMoreTokens()) {
                argsLst.add(tokenizer.nextToken());
            }
            if (argsLst.size() > 0) {
                // upstream cast the Object[] of toArray() to String[], which always throws
                return argsLst.toArray(new String[0]);
            }
        }
        return new String[0];
    }

    private @Nullable VirtualFile getSourceRoot(@Nullable final Module module) {
        if (module != null) {
            final VirtualFile[] sourcesRoots = ModuleRootManager.getInstance(module)
                .getContentFolderFiles(LanguageContentFolderScopes.productionAndTest());
            if (sourcesRoots.length >= 1) {
                return sourcesRoots[0];
            }
        }
        return null;
    }

    private String getOutputFilePath(final Module module) {
        final String filename = Platform.current().os().isWindows() ?
            module.getName() + ".exe" :
            module.getName();

        final @Nullable String outputDirUrl = getOutputDir(module);

        // try and get compiler output dir but it's likely to be null so default to project root
        final File outputFile = StringUtil.isNotEmpty(outputDirUrl) ?
            new File(VirtualFileUtil.urlToPath(outputDirUrl), filename) :
            new File(module.getProject().getBasePath(), filename);

        return outputFile.getPath();
    }

    private @Nullable String getOutputDir(final Module module) {
        return ModuleCompilerPathsManager.getInstance(module)
                        .getCompilerOutputUrl(ProductionContentFolderTypeProvider.getInstance());
    }

    public DlangRunAppConfiguration getConfig() {
        return config;
    }
}

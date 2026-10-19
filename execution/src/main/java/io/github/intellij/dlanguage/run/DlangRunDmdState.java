package io.github.intellij.dlanguage.run;

import consulo.content.bundle.Sdk;
import consulo.d.execution.DExecutionNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.localize.ProcessLocalize;
import consulo.project.ui.notification.NotificationService;
import consulo.util.lang.StringUtil;
import io.github.intellij.dlanguage.DlangSdkType;
import io.github.intellij.dlanguage.run.exception.ModuleNotFoundException;
import io.github.intellij.dlanguage.run.exception.NoSourcesException;
import io.github.intellij.dlanguage.run.exception.NoValidDlangSdkFound;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

public class DlangRunDmdState extends CommandLineState {

    private static final Logger LOG = Logger.getInstance(DlangRunDmdState.class);

    private final DlangRunDmdConfiguration config;

    DlangRunDmdState(final ExecutionEnvironment environment, final DlangRunDmdConfiguration config) {
        super(environment);
        this.config = config;
    }

    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        try {
            final GeneralCommandLine cmd = getDlangCompilerCommandLine(config);
            final ProcessHandler handler = config.getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
                .newBuilder(cmd)
                .colored()
                .build();
            ProcessTerminatedListener.attach(handler, config.getProject());
            return handler;
        } catch (final NoValidDlangSdkFound e) {
            throw new ExecutionException(DLocalize.runDmdNoValidSdk().get());
        } catch (final NoSourcesException e) {
            throw new ExecutionException(DLocalize.runDmdNoSources(String.valueOf(e.getSourcesRoot())).get());
        } catch (final ModuleNotFoundException e) {
            throw new ExecutionException(DLocalize.runNoModuleSelected().get());
        } catch (final ExecutionException e) {
            final String message = String.valueOf(e.getMessage());
            final boolean isEmpty = message.equals(ProcessLocalize.runConfigurationErrorExecutableNotSpecified().get());
            final boolean notCorrect = message.startsWith("Cannot run program");
            if (isEmpty || notCorrect) {
                NotificationService.getInstance()
                    .newError(DExecutionNotificationGroupContributor.DMD_RUN_CONFIGURATION)
                    .title(DLocalize.runDmdNotificationTitle())
                    .content(isEmpty ? DLocalize.runDmdNotificationCompilerEmpty() : DLocalize.runDmdNotificationCompilerNotCorrect())
                    .notify(config.getProject());
            }
            throw e;
        }
    }

    /**
     * Build command line:
     * <code>dmd -release -unittest -od{objFilesDir} -of{outputFilePath} sourceFile1.d sourceFile2.d</code>
     */
    private GeneralCommandLine getDlangCompilerCommandLine(final DlangRunDmdConfiguration config)
        throws ModuleNotFoundException, NoValidDlangSdkFound, NoSourcesException, ExecutionException {
        final @Nullable Module module = config.getConfigurationModule().getModule();
        if (module == null) {
            throw new ModuleNotFoundException();
        }

        if (module.isDisposed()) {
            LOG.warn("should not run D compiler as module is disposed");
            throw new ModuleNotFoundException("module is disposed");
        }

        final @Nullable Sdk sdk = ModuleUtilCore.getSdk(module, DModuleExtension.class);

        if(sdk != null && DlangSdkType.class.isAssignableFrom(sdk.getSdkType().getClass())) {
            final DlangSdkType dlangSdkType = (DlangSdkType) sdk.getSdkType();

            final String dCompilerPath = dlangSdkType.getDlangCompilerPath(sdk);

            if (StringUtil.isEmptyOrSpaces(dCompilerPath)) {
                throw new ExecutionException(DLocalize.runDmdCompilerNotSpecified().get());
            }
            if (!Paths.get(dCompilerPath).toFile().canExecute()) {
                throw new ExecutionException(DLocalize.runDmdCompilerNotConfigured().get());
            }

            final List<String> dmdParams = DlangDmdConfigToArgsConverter.getDmdParameters(config, module);

            final GeneralCommandLine cmd = new GeneralCommandLine()
                .withWorkDirectory(config.getProject().getBasePath())
                .withCharset(Charset.defaultCharset())
                .withExePath(dCompilerPath)
                .withParameters(dmdParams);

            LOG.debug(String.format("D compiler command: %s", cmd.getCommandLineString()));
            return cmd;
        } else {
            LOG.warn("No valid D compiler found");
            throw new NoValidDlangSdkFound();
        }
    }

}

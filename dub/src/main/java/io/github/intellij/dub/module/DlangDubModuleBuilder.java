package io.github.intellij.dub.module;

import consulo.application.Application;
import consulo.d.dub.DubNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersList;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationService;
import consulo.project.ui.notification.NotificationType;
import consulo.util.dataholder.Key;
import io.github.intellij.dlanguage.module.DlangModuleBuilder;
import io.github.intellij.dub.run.DlangRunDubConfigurationType;
import io.github.intellij.dub.service.DubBinaryPathProvider;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DlangDubModuleBuilder extends DlangModuleBuilder {
    private static final Logger LOG = Logger.getInstance(DlangDubModuleBuilder.class);
    private static final String RUN_DUB_CONFIG_NAME = "Run DUB";
    public static final String BUILDER_ID = "DLangDubApp";

    private @Nullable List<String> sourcePaths;
    private Map<String, String> dubOptions = new HashMap<>();

    public DlangDubModuleBuilder() {
        super(BUILDER_ID, DLocalize.moduleTitle(), DLocalize.moduleDubDescription());
    }

    /*
    * When creating a project the following are called in order:
    *   createProject()
    *   commitModule()
    *   createAndCommitIfNeeded()
    *   setupRootModel()
    *   getSourcePaths()
    *
    * The code for running "dub init" has been moved here so that it has access to the Project and the "source"
    * directory should be created prior to the call to getSourcePaths().
    *
    * Consulo: this is upstream's createAndCommitIfNeeded. The new module wizard sets up the root model first and calls
    * this once the project is open, from a background task, so "dub init" runs in the current thread.
    */
    public void runDubInit(Project project) {
        if (!dubOptions.isEmpty() && DubBinaryPathProvider.INSTANCE.isDubAvailable()) {
            try {
                GeneralCommandLine cmd = createDubInitCommand(getContentEntryPath());
                runDubCommand(project, cmd);
            }
            catch (Exception e) {
                LOG.error("dub init failed!", e);
            }
        }
    }

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) {
        super.setupRootModel(rootModel);
        LOG.debug("Setting up dub-specific root model for Dlang project");
    }

    /**
     * Consulo: the part of upstream's setupRootModel creating the "Run DUB" configuration; the new module wizard calls
     * it once the project is open and the module has its DUB package name.
     */
    public void setupRunConfiguration(Project project, Module module) {
        RunManager runManager = RunManager.getInstance(project);

        //Create "Run dub" configuration
        RunnerAndConfigurationSettings runDubSettings = runManager.findConfigurationByName(RUN_DUB_CONFIG_NAME);
        if (runDubSettings == null) {
            DlangRunDubConfigurationType configurationType = DlangRunDubConfigurationType.getInstance();
            ConfigurationFactory factory = configurationType.getConfigurationFactories()[0];
            RunnerAndConfigurationSettings settings = runManager.createRunConfiguration(RUN_DUB_CONFIG_NAME, factory);
            ((ModuleBasedConfiguration<?>) settings.getConfiguration()).setModule(module);
            runManager.addConfiguration(settings, false);
            if (runManager.getSelectedConfiguration() == null) {
                runManager.setSelectedConfiguration(settings);
            }
            LOG.debug(String.format("Run Configuration added for '%s'", RUN_DUB_CONFIG_NAME));
        }
    }

    /*
     * By default, dub init will create a directory named "source" so this method presumptuously
     * returns "{WORKING_DIR}/source" as the source path. This could result in errors if dub
     * where to start creating files using an alternative directory name.
     */
    @Override
    public @Nullable List<String> getSourcePaths() {
        List<String> paths = sourcePaths;
        if (paths == null || paths.isEmpty()) {
            String path = getContentEntryPath() + File.separator + "source";
            paths = new ArrayList<>(List.of(path)); // may need to add additional source paths later
            sourcePaths = paths;
        }
        return paths;
    }

    public void addSourcePath(String sourcePathInfo) {
        List<String> paths = sourcePaths;
        if (paths == null) {
            paths = new ArrayList<>();
            sourcePaths = paths;
        }
        paths.add(sourcePathInfo);
    }

    public void setDubOptions(Map<String, String> options) {
        dubOptions = options;
    }

    /*
    * Build up the "dub init" command based on the parameters defined during project creation
    */
    private GeneralCommandLine createDubInitCommand(@Nullable String workingDirectory) {
        ParametersList params = new ParametersList();
        params.addParametersString("init");
        params.addParametersString("-n");
        String dubParams = dubOptions.get("dubParams");
        if ((dubParams != null ? dubParams : "").isEmpty()) {
            params.addParametersString("--format");
            String dubFormat = dubOptions.get("dubFormat");
            params.addParametersString(dubFormat != null ? dubFormat : "json");
            params.addParametersString("--type");
            String dubType = dubOptions.get("dubType");
            params.addParametersString(dubType != null ? dubType : "minimal");
        }
        else {
            params.addParametersString(dubParams);
        }
        String dubBinary = DubBinaryPathProvider.INSTANCE.getDubPath();
        return new GeneralCommandLine()
            .withWorkDirectory(workingDirectory)
            .withExePath(Objects.requireNonNull(dubBinary))
            .withParameters(params.getParameters());
    }

    /*
    * Must not be run within the EDT
    */
    private void runDubCommand(Project project, GeneralCommandLine cmd) {
        LOG.debug(cmd.getCommandLineString());
        try {
            ProcessHandler process = Application.get().getInstance(ProcessHandlerBuilderFactory.class).newBuilder(cmd).build();
            DubInitListener listener = new DubInitListener();
            process.addProcessListener(listener);
            process.startNotify();
            process.waitFor();

            NotificationService.getInstance()
                .newOfType(
                    DubNotificationGroupContributor.GROUP,
                    listener.hasErrors() ? NotificationType.WARNING : NotificationType.INFORMATION
                )
                .title(DLocalize.dubInitNotificationTitle())
                .content(LocalizeValue.of(listener.getOutput()))
                .notify(project);
            if (listener.hasErrors()) {
                LOG.warn(listener.getOutput());
            }
        }
        catch (ExecutionException e) {
            LOG.warn("There was a problem running 'dub init'", e);
        }
    }

    private static class DubInitListener implements ProcessListener {
        private final StringBuffer buffer = new StringBuffer(); // use StringBuffer because StringBuilder is not thread safe
        private final AtomicBoolean errors = new AtomicBoolean();

        public String getOutput() {
            return buffer.toString();
        }

        public boolean hasErrors() {
            return errors.get();
        }

        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
            if (ProcessOutputTypes.STDERR == outputType) {
                errors.set(true);
            }
            if (ProcessOutputTypes.SYSTEM == outputType) {
                return;
            }
            buffer.append(event.getText());
        }
    }
}

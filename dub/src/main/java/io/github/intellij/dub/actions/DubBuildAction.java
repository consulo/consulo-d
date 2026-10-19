package io.github.intellij.dub.actions;

import consulo.annotation.component.ActionImpl;
import consulo.d.localize.DLocalize;
import consulo.execution.ExecutionUtil;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.executor.DefaultRunExecutor;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import io.github.intellij.dub.run.DlangRunDubConfiguration;
import io.github.intellij.dub.run.DlangRunDubConfigurationType;

/**
 * @author Samael Bate (singingbush)
 * created on 31/03/2017
 */
@ActionImpl(id = "dub.build.action")
public class DubBuildAction extends DubAction {

    private static final Logger LOG = Logger.getInstance(DubBuildAction.class);

    public DubBuildAction() {
        super(DLocalize.actionDubBuildText(), DLocalize.actionDubBuildDescription(), PlatformIconGroup.actionsExecute());
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(Project.KEY);
        if (project != null) {
            RunManager runManager = RunManager.getInstance(project);
            String projectName = project.getName();
            String configName = projectName + " [dub run]";

            RunnerAndConfigurationSettings runDubSettings = runManager.findConfigurationByName(configName);

            if (runDubSettings == null) {
                DlangRunDubConfigurationType runDubConfigurationType = DlangRunDubConfigurationType.getInstance();
                ConfigurationFactory factory = runDubConfigurationType.getConfigurationFactories()[0];
                runDubSettings = runManager.createRunConfiguration(configName, factory);

                DlangRunDubConfiguration config = (DlangRunDubConfiguration) runDubSettings.getConfiguration();

                config.setGeneralDubOptions(1); // 0: build, 1: run, 2: test

                // todo: find a way for the ToggleOffline action to set the following:
                //config.isCbNoDeps = true // '--nodeps' -> "Do not check/update dependencies before building"
                //config.additionalParams = "--skip-registry=all" // currently no config option for '--skip-registry={none|standard|all}'

                for (Module module : ModuleManager.getInstance(project).getModules()) {
                    if (module.getName().equals(projectName)) {
                        config.setModule(module);
                        break;
                    }
                }

                runManager.addConfiguration(runDubSettings);
            }

            runManager.setSelectedConfiguration(runDubSettings);

            // now actually start the process
            // Consulo: upstream ran it with its DubBuildRunner, here the run configuration's own runner starts it
            ExecutionUtil.runConfiguration(runDubSettings, DefaultRunExecutor.getRunExecutorInstance());
        }
    }
}

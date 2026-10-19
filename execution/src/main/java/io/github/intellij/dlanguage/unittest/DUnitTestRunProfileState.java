package io.github.intellij.dlanguage.unittest;

import consulo.application.Application;
import consulo.disposer.Disposer;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.ExecutionResult;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.ProgramRunner;
import consulo.execution.test.sm.SMTestRunnerConnectionUtil;
import consulo.execution.test.sm.runner.SMTRunnerConsoleProperties;
import consulo.execution.test.sm.runner.SMTestLocator;
import consulo.execution.test.sm.ui.SMTRunnerConsoleView;
import consulo.process.ExecutionException;
import consulo.project.Project;
import io.github.intellij.dlanguage.DLanguage;
import org.jspecify.annotations.Nullable;

public class DUnitTestRunProfileState implements RunProfileState {

    private final ExecutionEnvironment environment;

    DUnitTestRunProfileState(final ExecutionEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public @Nullable ExecutionResult execute(final Executor executor, final ProgramRunner programRunner) throws ExecutionException {
        final Project project = environment.getProject();

        final @Nullable RunnerAndConfigurationSettings settings = environment.getRunnerAndConfigurationSettings();

        if (settings == null) {
//            handleError(project, "No runner and configuration settings found.");
            return null;
        }

        final RunConfiguration configuration = settings.getConfiguration();

        if (!(configuration instanceof DUnitTestRunConfiguration)) {
//            handleError(project, "The supplied configuration is not a " + DUnitTestRunConfiguration.class.getSimpleName() + ".");
            return null;
        }

        final DUnitTestRunConfiguration unitTestRunConfiguration = (DUnitTestRunConfiguration) configuration;

        // Create the test runner
        final DUnitTestRunProcessHandler processHandler = new DUnitTestRunProcessHandler(project, unitTestRunConfiguration);

        final SMTRunnerConsoleProperties properties = new SMTRunnerConsoleProperties(configuration, DLanguage.INSTANCE.getDisplayName().get(), executor) {
            @Override
            public SMTestLocator getTestLocator() {
                return DUnitTestLocationProvider.getInstance();
            }
        };

//        properties.addStackTraceFilter(new ApexStackTraceFilter());

        final SMTRunnerConsoleView consoleView = (SMTRunnerConsoleView) SMTestRunnerConnectionUtil
            .createAndAttachConsole(DLanguage.INSTANCE.getDisplayName().get(), processHandler, properties);

        Disposer.register(project, consoleView);

        // Integrated code coverage
        // NOTE: Only necessary if you’re going to add code coverage support, and that’s a TOTALLY different discussion!
//        CoverageHelper.attachToProcess(unitTestRunConfiguration, processHandler, getRunnerSettings());

        // Register an action to re-run failed tests
        final DUnitTestRerunFailedTestsAction rerunFailedTestsAction = new DUnitTestRerunFailedTestsAction(project, consoleView);
        rerunFailedTestsAction.init(consoleView.getProperties());
        rerunFailedTestsAction.setModelProvider(consoleView::getResultsViewer);

        final DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
        executionResult.setRestartActions(rerunFailedTestsAction);

        // Start the process handler
        Application.get()
            .invokeLater(processHandler::startProcessing);

        return executionResult;
    }

//    @Override
//    public RunnerSettings getRunnerSettings()
//    {
//        return environment.getRunnerSettings();
//    }
//
//    @Override
//    public ConfigurationPerRunnerSettings getConfigurationSettings()
//    {
//        return environment.getConfigurationSettings();
//    }
}

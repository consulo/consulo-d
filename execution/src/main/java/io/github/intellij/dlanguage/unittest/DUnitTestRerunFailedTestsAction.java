package io.github.intellij.dlanguage.unittest;

import consulo.execution.action.Location;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.test.AbstractTestProxy;
import consulo.execution.test.action.AbstractRerunFailedTestsAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.ui.ex.ComponentContainer;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.MultiMap;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class DUnitTestRerunFailedTestsAction extends AbstractRerunFailedTestsAction {
    private final Project project;

    public DUnitTestRerunFailedTestsAction(final Project project, final ComponentContainer componentContainer) {
        super(componentContainer);
        this.project = project;
    }

    @Override
    protected @Nullable MyRunProfile getRunProfile(final ExecutionEnvironment environment) {
        final Project project = environment.getProject();

        return project != null? getRunProfile(project) : null;
    }

    private @Nullable MyRunProfile getRunProfile(final Project project) {
        final List<AbstractTestProxy> failedTests = getFailedTests(project);
        if (ContainerUtil.isEmpty(failedTests))
        {
            return null;
        }

        final MultiMap<Module, String> moduleToFailedTestNames = MultiMap.createLinked();
        final GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        for (final AbstractTestProxy failedTest : failedTests) {
            final Location location = failedTest.getLocation(project, searchScope);
            if (location != null) {
                final PsiElement psiElement = location.getPsiElement();
                // TODO: Use the associated element to determine the failed test method/class
//                moduleToFailedTestNames.putValue(module, failedTestName);
            }
        }

        if (moduleToFailedTestNames.isEmpty()) {
            return null;
        }

        return new MyRunProfile(new DUnitTestRunConfiguration(project)) {
            @Override
            public Module[] getModules() {
                final Set<Module> modules = moduleToFailedTestNames.keySet();
                return modules.toArray(new Module[modules.size()]);
            }

            @Override
            public @Nullable RunProfileState getState(final Executor executor,
                                                      final ExecutionEnvironment environment) throws ExecutionException {
                return new DUnitTestRunProfileState(environment);
//                return new DUnitTestRunProfileState(environment, moduleToFailedTestNames);
            }
        };
    }
}

package io.github.intellij.dlanguage.unittest;

import consulo.application.Application;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.library.Library;
import consulo.content.library.LibraryTable;
import consulo.content.library.LibraryTablesRegistrar;
import consulo.d.execution.DExecutionNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.test.sm.ServiceMessageBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.process.BaseProcessHandler;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationService;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import io.github.intellij.dlanguage.psi.DLanguageAtAttribute;
import io.github.intellij.dlanguage.psi.DLanguageTemplateMixin;
import io.github.intellij.dlanguage.psi.named.DLanguageClassDeclaration;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;
import io.github.intellij.dlanguage.settings.ToolKey;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DUnitTestRunProcessHandler extends BaseProcessHandler { // consider OSProcessHandler or BaseProcessHandler<Process>

    private static final Logger LOG = Logger.getInstance(DUnitTestRunProcessHandler.class);
    private final Map<String, Integer> nodeIdsByFullTestName = new HashMap<>();
    private final Project project;
    private final DUnitTestRunConfiguration configuration;
    private final Map<String, Set<String>> testClassToTestMethodNames = new HashMap<>();
    private int nextNodeId = 0;


    public DUnitTestRunProcessHandler(final Project project, final DUnitTestRunConfiguration configuration) {
        this.project = project;
        this.configuration = configuration;
    }

    public void startProcessing() {
        // NOTE: After this, actual run your tests and, as results come back, call:
//   testFinished() when a test method finishes successfully
//   testFailed() when a test method finishes with a failure
//   testIgnored() when a test method is skipped for any reason
//   testSuiteFinished() when all methods in a test class finish
//   testRunFinished() when all tests have completed
//   testRunCancelled() if the user has canceled the run if you need to clean up anything
//   testStdOut() to print the output from a test class/method to the console view for review
//            }
        Application.get().runReadAction(() -> {

            // NOTE: This tells the test runner that we’re actually going to do something
            testRunStarted();

            // NOTE: This creates the tree in the UI by sending testSuiteStarted() and testStarted() messages
            final DUnitTestFramework unitTestFramework = new DUnitTestFramework();

            try {
                final PsiFile psiFile = PsiManager.getInstance(project).findFile(configuration.getDFile());
                final Collection<DLanguageClassDeclaration> cds = PsiTreeUtil.findChildrenOfType(psiFile, DLanguageClassDeclaration.class);

                for (final DLanguageClassDeclaration cd : cds) {

                    // if a class contains the UnitTest mixin assume its a valid d-unit test class
                    String testClassName = null;
                    final Collection<DLanguageTemplateMixin> tmis = PsiTreeUtil.findChildrenOfType(cd, DLanguageTemplateMixin.class);
                    for (final DLanguageTemplateMixin tmi : tmis) {
                        if (tmi.getText().contains("UnitTest")) {
                            final PsiElement classIdentifier = cd.getIdentifier();
                            if (classIdentifier != null) {
                                testClassName = classIdentifier.getText();
                            }
                        }
                    }

                    // find methods for the class
                    final Set<String> testMethodNames = new LinkedHashSet<>();
                    final Collection<DLanguageFunctionDeclaration> fds = PsiTreeUtil
                        .findChildrenOfType(cd, DLanguageFunctionDeclaration.class);
                    for (final DLanguageFunctionDeclaration fd : fds) {
                        if (testClassName != null) {
                            // only add methods with @Test
                            if (isTestMethod(fd) || isIgnoreMethod(fd)) {
                                testMethodNames.add(Objects.requireNonNull(fd.getIdentifier()).getText());
                            }
                        }
                    }

                    // add class and corresponding methods to the map
                    if (testClassName != null && !testMethodNames.isEmpty()) {
                        testClassToTestMethodNames.put(testClassName, testMethodNames);
                    }
                }

            } catch (final RuntimeConfigurationError runtimeConfigurationError) {
                runtimeConfigurationError.printStackTrace();
            }
        });

        // get d-unit path
        final String dunitPath = getDUnitPath();

        if (dunitPath == null) {
            final String message = DLocalize.dUiUnittestNotificationContentDUnitMissing().get();
            NotificationService.getInstance()
                .newError(DExecutionNotificationGroupContributor.EXECUTE_TESTS)
                .title(DLocalize.dUiUnittestNotificationTitleDUnitMissing())
                .content(DLocalize.dUiUnittestNotificationContentDUnitMissing())
                .notify(project);
            LOG.warn(message);

            testRunCancelled();
        } else {
            // Actually run tests in separate runnable to avoid blocking the GUI
            Application.get().executeOnPooledThread(() -> {
                for (final Map.Entry<String, Set<String>> pair : testClassToTestMethodNames.entrySet()) {
                    final String className = pair.getKey();
                    final Set<String> methodNames = pair.getValue();

                    testSuiteStarted(className, methodNames.size());
                    long startTime = System.currentTimeMillis();

                    for (final String testMethodName : methodNames) {
                        testStarted(className, testMethodName);
                        executeTest(className, testMethodName, dunitPath);
                    }

                    long duration = (System.currentTimeMillis() - startTime);
                    testSuiteFinished(className, duration);
                }

                testRunFinished();
            });
        }
    }

    private @Nullable String getDUnitPath() {
        final LibraryTable projectLibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);

        final Optional<Library> dunit = Arrays.stream(projectLibraryTable.getLibraries())
            .filter(lib -> StringUtil.isNotEmpty(lib.getName()) && Objects.requireNonNull(lib.getName()).contains("d-unit-")) // todo: also check for "dunit-" ??
            .findFirst(); // it's possible for a dependency to be found multiple times

        if(dunit.isPresent()) {
            final VirtualFile[] files = dunit.get().getFiles(BinariesOrderRootType.ID);
            if (files.length > 0) {
                return dubImportPath(Objects.requireNonNull(files[0].getCanonicalPath()));
            }
        }

        return null;
    }

    private @Nullable String dubImportPath(final String rootPath) {
        final String pathUrl = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, rootPath);
        final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(pathUrl);
        if (file == null) {
            return null;
        }
        final List<String> sourcesDir = new ArrayList<>();
        VirtualFileUtil.visitChildrenRecursively(file, new VirtualFileVisitor<Object>() {
            @Override
            public boolean visitFile(final VirtualFile file) {
                if (file.isDirectory()) {
                    if (file.getName().equals("source")) {
                        sourcesDir.add("source");
                    }
                    if (file.getName().equals("src")) {
                        sourcesDir.add("src");
                    }
                }
                return true;
            }
        });
        return sourcesDir.isEmpty() ? null : rootPath + File.separator + sourcesDir.get(0); // todo: may not need 'File.separator' can prob use / on all platforms
    }


    private void executeTest(final String className, final String testMethodName, final String dunitPath) {
        final String testPath = className + "." + testMethodName;
        final String workingDirectory = project.getBasePath();
        //            final String testFile = configuration.getDFile().getCanonicalPath();

        // FIXME this use the raw value so do not work if dub path is empty
        final String dubPath = ToolKey.DUB_KEY.getPath();
        if (dubPath == null || dubPath.isEmpty()) {
            NotificationService.getInstance()
                .newWarn(DExecutionNotificationGroupContributor.DUNIT_TEST_RUNNER)
                .title(DLocalize.dUiUnittestNotificationTitleDubPathMissing())
                .content(DLocalize.dUiUnittestNotificationContentDubPathMissing())
                .notify(project);
            return;
        }

        final GeneralCommandLine cmd = new GeneralCommandLine()
            .withWorkDirectory(workingDirectory)
            .withCharset(Charset.defaultCharset())
            .withExePath(dubPath)
            .withParameters("test", "--", "-v", "--filter", testPath + "$");

        long startTime = System.currentTimeMillis();

        final StringBuilder builder = new StringBuilder();
        try {
            final ProcessHandler process = Application.get().getInstance(ProcessHandlerBuilderFactory.class)
                .newBuilder(cmd)
                .build();
            process.addProcessListener(new ProcessListener() {
                @Override
                public void onTextAvailable(final ProcessEvent event, final Key outputType) {
                    builder.append(event.getText());
                }
            });

            process.startNotify();
            process.waitFor();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }

        final String result = builder.toString();
        // call either finished(success) or failed
        final String successPatternString = ".*OK:.*";
        final Pattern successPattern = Pattern.compile(successPatternString);
        final Matcher successMatcher = successPattern.matcher(result);

        long duration = System.currentTimeMillis() - startTime;

        if (successMatcher.find()) {
            testFinished(className, testMethodName, duration);
            testStdOut(className, testMethodName, result);
        } else if (result.contains("FAILURE:")) {
            testFailed(className, testMethodName, duration, "Failed", result);
        } else if (result.contains("SKIP:")) {
            testIgnored(className, testMethodName);
            testStdOut(className, testMethodName, result);
        } else {
            testFailed(className, testMethodName, duration, "Failed for unknown reasons", result);
        }

    }

    private boolean isTestMethod(final DLanguageFunctionDeclaration fd) {
        final DLanguageAtAttribute ele = findElementUpstream(fd, DLanguageAtAttribute.class);
        return (ele != null && ele.getText().contains("@Test"));
    }

    private boolean isIgnoreMethod(final DLanguageFunctionDeclaration fd) {
        final DLanguageAtAttribute psiElement = findElementUpstream(fd, DLanguageAtAttribute.class);
        return (psiElement != null && psiElement.getText().contains("@Ignore"));
    }

    private <T extends PsiElement> @Nullable T findElementUpstream(final PsiElement startingElement, final Class<T> clazz) {
        final PsiElement parent = startingElement.getParent();
        if (parent == null) {
            return null;
        }
        final T element = PsiTreeUtil.findChildOfType(parent, clazz);
        if (clazz.isInstance(element)) {
            return element;
        } else {
            try {
                return findElementUpstream(parent, clazz);
            } catch (final Exception e) {
                return null;
            }
        }
    }

    private synchronized int getNodeId(final String fullTestName) {
        Integer nodeId = nodeIdsByFullTestName.get(fullTestName);
        if (nodeId == null) {
            nodeId = nextNodeId++;
            nodeIdsByFullTestName.put(fullTestName, nodeId);
        }
        return nodeId;
    }

    private void testRunStarted() {
        Application.get()
            .invokeLater(() -> notifyTextAvailable(new ServiceMessageBuilder("enteredTheMatrix").toString() + "\n", ProcessOutputTypes.STDOUT));
    }

    private void testRunFinished() {
        Application.get()
            .invokeLater(this::destroyProcess);
    }

    private void testRunCancelled() {
        Application.get()
            .invokeLater(this::destroyProcess);
    }

    private void testStdOut(final String testClassName, final String output) {
        Application.get()
            .invokeLater(() -> notifyTextAvailable(ServiceMessageBuilder
                        .testStdOut(testClassName)
                        .addAttribute("out", output)
                        .addAttribute("nodeId", String.valueOf(getNodeId(testClassName)))
                        .toString() + "\n",
                ProcessOutputTypes.STDOUT));
    }

    private void testStdOut(final String testClassName, final String testMethodName, final String output) {
        Application.get()
            .invokeLater(() -> {
            final String fullTestMethodName = testClassName + "." + testMethodName;
            notifyTextAvailable(ServiceMessageBuilder
                            .testStdOut(testMethodName)
                            .addAttribute("out", output)
                            .addAttribute("parentNodeId", String.valueOf(getNodeId(testClassName)))
                            .addAttribute("nodeId", String.valueOf(getNodeId(fullTestMethodName)))
                            .toString() + "\n",
                    ProcessOutputTypes.STDOUT);
        });
    }

    private void testSuiteStarted(final String testClassName, final int numTestMethods) {
        Application.get()
            .invokeLater(() -> notifyTextAvailable(ServiceMessageBuilder
                        .testSuiteStarted(testClassName)
                        .addAttribute("isSuite", String.valueOf(true))
                        .addAttribute("parentNodeId", String.valueOf(getNodeId("")))
                        .addAttribute("nodeId", String.valueOf(getNodeId(testClassName)))
                        .addAttribute("count", String.valueOf(numTestMethods))
                        .addAttribute("locationHint", DUnitTestLocationProvider.PROTOCOL_PREFIX + testClassName)
                        .toString() + "\n",
                ProcessOutputTypes.STDOUT));
    }

    private void testSuiteFinished(final String testClassName, final long duration) {
        Application.get()
            .invokeLater(() -> notifyTextAvailable(ServiceMessageBuilder
                        .testSuiteFinished(testClassName)
                        .addAttribute("isSuite", String.valueOf(true))
                        .addAttribute("parentNodeId", String.valueOf(getNodeId("")))
                        .addAttribute("nodeId", String.valueOf(getNodeId(testClassName)))
                        .addAttribute("duration", String.valueOf(duration))
                        .addAttribute("locationHint", DUnitTestLocationProvider.PROTOCOL_PREFIX + testClassName)
                        .toString() + "\n",
                ProcessOutputTypes.STDOUT));
    }

    private void testStarted(final String testClassName, final String testMethodName) {
        Application.get()
            .invokeLater(() -> {
                final String fullTestMethodName = testClassName + "." + testMethodName;
            notifyTextAvailable(ServiceMessageBuilder
                            .testStarted(testMethodName)
                            .addAttribute("isSuite", String.valueOf(false))
                            .addAttribute("parentNodeId", String.valueOf(getNodeId(testClassName)))
                            .addAttribute("nodeId", String.valueOf(getNodeId(fullTestMethodName)))
                            .addAttribute("locationHint", DUnitTestLocationProvider.PROTOCOL_PREFIX + testClassName + "/" + testMethodName)
                            .toString() + "\n",
                    ProcessOutputTypes.STDOUT);
        });
    }

    private void testFinished(final String testClassName, final String testMethodName, final long duration) {
        Application.get()
            .invokeLater(() -> {
            final String fullTestMethodName = testClassName + "." + testMethodName;
            notifyTextAvailable(ServiceMessageBuilder
                            .testFinished(testMethodName)
                            .addAttribute("isSuite", String.valueOf(false))
                            .addAttribute("parentNodeId", String.valueOf(getNodeId(testClassName)))
                            .addAttribute("nodeId", String.valueOf(getNodeId(fullTestMethodName)))
                            .addAttribute("duration", String.valueOf(duration))
                            .addAttribute("locationHint", DUnitTestLocationProvider.PROTOCOL_PREFIX + testClassName + "/" + testMethodName)
                            .toString() + "\n",
                    ProcessOutputTypes.STDOUT);
        });
    }

    private void testFailed(final String testClassName, final String testMethodName, final long duration, final String message, final String stackTrace) {
        Application.get()
            .invokeLater(() -> {
            final String fullTestMethodName = testClassName + "." + testMethodName;
            notifyTextAvailable(ServiceMessageBuilder
                            .testFailed(testMethodName)
                            .addAttribute("isSuite", String.valueOf(false))
                            .addAttribute("parentNodeId", String.valueOf(getNodeId(testClassName)))
                            .addAttribute("nodeId", String.valueOf(getNodeId(fullTestMethodName)))
                            .addAttribute("duration", String.valueOf(duration))
                            .addAttribute("message", message)
                            .addAttribute("details", stackTrace)
                            .addAttribute("locationHint", DUnitTestLocationProvider.PROTOCOL_PREFIX + testClassName + "/" + testMethodName)
                            .toString() + "\n",
                    ProcessOutputTypes.STDOUT);
        });
    }

    private void testIgnored(final String testClassName, final String testMethodName) {
        Application.get()
            .invokeLater(() -> {
            final String fullTestMethodName = testClassName + "." + testMethodName;
            notifyTextAvailable(ServiceMessageBuilder
                            .testIgnored(testMethodName)
                            .addAttribute("isSuite", String.valueOf(false))
                            .addAttribute("parentNodeId", String.valueOf(getNodeId(testClassName)))
                            .addAttribute("nodeId", String.valueOf(getNodeId(fullTestMethodName)))
                            .addAttribute("locationHint", DUnitTestLocationProvider.PROTOCOL_PREFIX + testClassName + "/" + testMethodName)
                            .toString() + "\n",
                    ProcessOutputTypes.STDOUT);
        });
    }

    @Override
    protected void destroyProcessImpl() {
        testRunCancelled();
        notifyProcessTerminated(0);
    }

    @Override
    protected void detachProcessImpl() {
        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return null;
    }
}

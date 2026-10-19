package io.github.intellij.dlanguage.actions;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.progress.Task;
import consulo.d.language.DFormatNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionHelper;
import consulo.execution.process.ExecutionModes;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.util.CapturingProcessAdapter;
import consulo.process.util.ProcessOutput;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationService;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithAsyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.coroutine.ActionSafeReadLock;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.lang.ExceptionUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import io.github.intellij.dlanguage.psi.DlangPsiFile;
import io.github.intellij.dlanguage.settings.ToolKey;
import org.jspecify.annotations.Nullable;

import java.nio.file.Paths;
import java.util.Objects;


/**
 * Action that calls Dfmt on the buffer it is invoked for.
 */
@ActionImpl(id = "DLanguage.DFormat", parents = @ActionParentRef(value = @ActionRef(id = "CodeFormatGroup"), anchor = ActionRefAnchor.LAST))
public class DFormatAction extends DumbAwareAction implements AnActionWithAsyncUpdate {
    private static final Logger LOG = Logger.getInstance(DFormatAction.class);

    public DFormatAction() {
        super(DLocalize.actionDlanguageDformatText(), DLocalize.actionDlanguageDformatDescription());
    }

    /**
     * Enable the action for D files.
     */
    @Override
    public Coroutine<?, ?> updateAsync(AnActionEvent e) {
        return ActionSafeReadLock.<Object, Object>run(e, presentation -> {
            PsiFile psiFile = e.getData(PsiFile.KEY);
            if (psiFile != null) {
                presentation.setEnabled(DlangPsiFile.class.isAssignableFrom(psiFile.getClass()));
            }
        }).toCoroutine();
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(AnActionEvent event) {
        PsiFile psiFile = event.getData(PsiFile.KEY);
        Project project = event.getData(Project.KEY);

        if (project == null || !(psiFile instanceof DlangPsiFile dlangPsiFile)) {
            return;
        }

        VirtualFile virtualFile = event.getData(VirtualFile.KEY);

        if (virtualFile == null || !virtualFile.isValid()) {
            // if the virtual file is no longer valid (eg: it's been deleted or changed) then there's no point continuing
            return;
        }

        // if we tell the editor to save the file, we can avoid reading in its contents and all the thread stuff that goes with it.
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document == null) {
            return;
        }
        FileDocumentManager.getInstance().saveDocument(document);

        // This will execute dfmt on another background thread that shouldn't throw up BGT too slow errors
        // and the best part? It'll give us a progress indicator!
        ProgressManager.getInstance().run(new Task.Backgroundable(project, DLocalize.dfmtProgressTitle(dlangPsiFile.getFullyQualifiedModuleName())) {
            @Override
            public void run(ProgressIndicator indicator) {
                GeneralCommandLine commandLine = createCommandLine(project, virtualFile);

                try {
                    // This particular process handle is quite brilliant.
                    // It allows the user to kill dfmt gracefully, and if that fails forcefully.
                    ProcessHandler processHandler = ProcessHandlerBuilder.create(Objects.requireNonNull(commandLine)).killable().build();

                    // we need process output from dfmt to have an idea of if it was succesfull and if not notify the user of stderr.
                    ProcessOutput processOutput = new ProcessOutput();
                    processHandler.addProcessListener(new CapturingProcessAdapter(processOutput) {
                        // when dfmt completes (for whatever reason), we'll learn of its error code and grab its stderr output
                        @Override
                        public void processTerminated(ProcessEvent event) {
                            super.processTerminated(event);

                            // first lets check to see if dfmt executed correctly
                            boolean success = event.getExitCode() == 0 && processOutput.getStderr().isEmpty();

                            if (!success) {
                                // this is the most interesting condition
                                // we need to log stderr
                                notifyOfError(DLocalize.dfmtNotificationFailed(psiFile.getName()), processOutput.getStderr(), project);
                                return;
                            }

                            notifyOfInformation(DLocalize.dfmtNotificationFormatted(psiFile.getName()), project);
                        }
                    });

                    processHandler.startNotify();

                    ExecutionModes.SameThreadMode sameThreadMode = new ExecutionModes.SameThreadMode("DFormat " + virtualFile.getCanonicalPath());
                    ExecutionHelper.executeExternalProcess(project, processHandler, sameThreadMode, commandLine);
                }
                catch (ExecutionException e) {
                    notifyOfError(DLocalize.dfmtNotificationFailed(psiFile.getName()), getUserStackTrace(e, LOG), project);
                    LOG.error(e);
                    return;
                }

                // this is expected to reload the editor automatically
                VirtualFileUtil.markDirtyAndRefresh(true, true, true, virtualFile);
            }
        });
    }

    @Nullable GeneralCommandLine createCommandLine(Project project, VirtualFile virtualFile) {
        String dfmtPath = ToolKey.DFORMAT_KEY.getPath();
        String dfmtFlags = ToolKey.DFORMAT_KEY.getFlags();

        if (dfmtPath == null || dfmtPath.isEmpty()) {
            notifyOfWarning(DLocalize.dfmtNotificationPathEmpty(), project);
            return null;
        }

        if (!Paths.get(dfmtPath).toFile().canExecute()) {
            notifyOfWarning(DLocalize.dfmtNotificationPathInvalid(), project);
            return null;
        }

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(dfmtPath);
        commandLine.getParametersList().addParametersString(dfmtFlags);

        if (!(dfmtFlags.contains("-i") || dfmtFlags.contains("--inplace"))) {
            // Do the work in place of the file system, rather than handle it with stdio (which could be slow)
            commandLine.addParameter("--inplace");
        }

        commandLine.getParametersList().addParametersString(Objects.requireNonNull(virtualFile.getCanonicalPath()));

        // Given the directory that the file that we are formatting is in
        //  set the current working directory of the processing being executed to it
        // This will allow .editorconfig to be searched for and be found in relation to the file.
        commandLine.setWorkDirectory(Objects.requireNonNull(virtualFile.getParent()).getCanonicalPath());
        return commandLine;
    }

    private void notifyOfInformation(LocalizeValue errorMessage, Project project) {
        NotificationService.getInstance()
            .newInfo(DFormatNotificationGroupContributor.GROUP)
            .title(DLocalize.dfmtNotificationTitle())
            .content(errorMessage)
            .notify(project);
    }

    // Consulo: upstream's "Configure" action (DToolsNotificationAction) is not ported yet
    private void notifyOfWarning(LocalizeValue errorMessage, Project project) {
        NotificationService.getInstance()
            .newWarn(DFormatNotificationGroupContributor.GROUP)
            .title(DLocalize.dfmtNotificationTitle())
            .content(errorMessage)
            .notify(project);
    }

    private void notifyOfError(LocalizeValue title, String errorMessage, Project project) {
        NotificationService.getInstance()
            .newError(DFormatNotificationGroupContributor.GROUP)
            .title(title)
            .content(LocalizeValue.of(errorMessage))
            .notify(project);
    }

    /**
     * IntelliJ's {@code ExceptionUtil.getUserStackTrace}, which is not in Consulo's API.
     */
    private static String getUserStackTrace(Throwable aThrowable, Logger logger) {
        String result = ExceptionUtil.getThrowableText(aThrowable, "com.intellij.");
        if (!result.contains("\n\tat")) {
            // no stack frames found
            logger.error(aThrowable);
        }
        return result;
    }
}

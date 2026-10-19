package io.github.intellij.dlanguage.highlighting.annotation.external;

import consulo.application.Application;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.d.language.DScannerNotificationGroupContributor;
import consulo.d.localize.DLocalize;
import consulo.d.module.extension.DModuleExtension;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersList;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.ui.notification.NotificationService;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import io.github.intellij.dlanguage.DlangSdkType;
import io.github.intellij.dlanguage.highlighting.annotation.DProblem;
import io.github.intellij.dlanguage.settings.ToolKey;
import io.github.intellij.dlanguage.utils.DUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DScanner implements DlangLinter {

    private static final Logger LOG = Logger.getInstance(DScanner.class);

    public DProblem[] checkFileSyntax(final PsiFile file) {
        if (file.getProject().isDisposed()) {
            LOG.debug("Won't check file syntax with DScanner as project is disposed");
            return new DProblem[] {};
        }

        if (!Objects.requireNonNull(file.getVirtualFile()).isValid()) {
            LOG.debug("Won't check file syntax with DScanner as VirtualFile is not valid");
            return new DProblem[] {};
        }

        final String dscannerPath = ToolKey.DSCANNER_KEY.getPath();
        if (dscannerPath == null || StringUtil.isEmpty(dscannerPath)) return new DProblem[] {};

        final GeneralCommandLine cmd = buildDscannerCommand(file, dscannerPath);
        try {
            return runDscannerCommand(file, cmd).toArray(new DProblem[] {});
        } catch (java.util.concurrent.ExecutionException | TimeoutException | InterruptedException e) {
            LOG.warn("Problem running DScanner", e);
        }
        return new DProblem[] {};
    }

    private GeneralCommandLine buildDscannerCommand(final PsiFile file, final String dscannerPath) {
        final String filePath = Objects.requireNonNull(Objects.requireNonNull(file.getVirtualFile()).getCanonicalPath());
        final String workingDirectory = file.getProject().getBasePath();

        final GeneralCommandLine cmd = new GeneralCommandLine()
            .withWorkDirectory(workingDirectory)
            .withExePath(dscannerPath);

        final ParametersList args = cmd.getParametersList();
        args.addParametersString("-S");
        args.addParametersString(filePath);

        final String flags = ToolKey.DSCANNER_KEY.getFlags();

        if (DUtil.isNotNullOrEmpty(flags)) {
            args.addAll(flags);
        }

        // try to auto add the compiler sources
        final List<String> compilerSources = getCompilerSourcePaths(file);
        for (final String s : compilerSources) {
            args.addParametersString("-I");
            args.addParametersString(s);
        }

        return cmd;
    }

    private List<Problem> runDscannerCommand(final PsiFile file, final GeneralCommandLine cmd) throws java.util.concurrent.ExecutionException, InterruptedException, TimeoutException {
        return Application.get().executeOnPooledThread(() -> {
            final List<Problem> problems = new ArrayList<>();

            try {
                LOG.trace("Starting DScanner process");

                final ProcessHandler process = ProcessHandlerBuilder.create(cmd).build();
                process.addProcessListener(new ProcessListener() {
                    @Override
                    public void onTextAvailable(final ProcessEvent event, final Key outputType) {
                        // we don't care about "system" or "stderr"
                        if(ProcessOutputTypes.STDOUT.equals(outputType)) {
                            parseProblem(event.getText(), file)
                                .ifPresent(problems::add);
                        } else if(ProcessOutputTypes.STDERR.equals(outputType)) {
                            LOG.warn(event.getText());
                            if (event.getText().contains("[error]")) {
                                NotificationService.getInstance()
                                    .newError(DScannerNotificationGroupContributor.GROUP)
                                    .title(DLocalize.dscannerError())
                                    .content(LocalizeValue.of(event.getText()))
                                    .notify(file.getProject());                            }
                        }
                    }
                });

                process.startNotify();
                process.waitFor(); // fails here if on wrong thread

                final Integer exitCode = process.getExitCode(); // 0 or 1 depending on whether DScanner found problems

                if(Integer.valueOf(1).equals(exitCode)) {
                    LOG.info(String.format("DScanner found %s lint problems in %s", problems.size(), file.getName()));
                }
            } catch (final ExecutionException e) {
                LOG.error("There was a problem running DScanner", e);
            }
            return problems;
        }).get(3, TimeUnit.SECONDS);
    }

    private List<String> getCompilerSourcePaths(final PsiFile file) {
        return Application.get().runReadAction((Supplier<List<String>>) () -> {
            @Nullable final Module module = ProjectRootManager.getInstance(file.getProject())
                .getFileIndex()
                .getModuleForFile(Objects.requireNonNull(file.getVirtualFile()));

            final ArrayList<String> compilerSourcePaths = new ArrayList<>();

            if(module != null && !module.isDisposed()) {
                @Nullable final Sdk sdk = ModuleUtilCore.getSdk(module, DModuleExtension.class);

                if (sdk != null && (sdk.getSdkType() instanceof DlangSdkType)) {
                    for (VirtualFile f: sdk.getSdkModificator().getRoots(SourcesOrderRootType.ID)) {
                        if (f.exists() && f.isDirectory()) {
                            compilerSourcePaths.add(f.getPath());
                        }
                    }
                }
            }

            return compilerSourcePaths;
        });
    }

    // Example output from DScanner:
    // hello.d(1:7)[error]: Expected identifier instead of ;
    // ddbc/source/ddbc/drivers/mysqlddbc.d(1273:35)[warn]: Line is longer than 120 characters
    private Optional<Problem> parseProblem(final String lint, final PsiFile file) {
        LOG.debug(lint);

        final Pattern p = Pattern.compile("\\w+\\.d\\((\\d+):(\\d+)\\)\\[(\\w+)\\]:(.+)");
        final Matcher m = p.matcher(lint);

        if (m.find()) {
            final int line = Integer.parseInt(m.group(1));
            final int column = Integer.parseInt(m.group(2));
            final TextRange range = LinterHelper.calculateTextRange(file, line, column);
            if (range == null) {
                LOG.warn("Unable to calculate text range for lint problem: " + lint);
                return Optional.empty();
            }
            final String severity = m.group(3);
            final String message = StringUtil.trim(m.group(4));

            final Problem problem = new Problem(range, message, severity);
            return Optional.of(problem);
        }
        return Optional.empty();
    }

    public static class Problem implements DProblem {
        private final String severity;
        private final TextRange range;
        private final String message;

        Problem(final TextRange range, final String message, final String severity) {
            this.range = range;
            this.severity = severity;
            this.message = message;
        }

        @Override
        public void createAnnotations(final PsiFile file, final AnnotationHolder holder) {
            if ("error".equals(severity)) {
                holder.newAnnotation(HighlightSeverity.ERROR, LocalizeValue.of(message)).range(range).create();
            } else if (message.contains("undocumented")) {
                holder.newAnnotation(HighlightSeverity.INFORMATION, LocalizeValue.of(message)).range(range).create();
            } else {
                holder.newAnnotation(HighlightSeverity.WARNING, LocalizeValue.of(message)).range(range).create();
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");
            sb.append(severity).append("] ")
                .append(range)
                .append(" ")
                .append(message);
            return sb.toString();
        }
    }

}
